/*
 * ============================================================
 *  TechStore · Panel de precios — lógica del frontend
 *
 *  IMPORTANTE: este archivo NUNCA contiene la API Key del proveedor.
 *
 *  Todo el código de un frontend (este archivo incluido) se descarga al
 *  navegador del visitante: cualquiera puede leerlo con F12 o "Ver código
 *  fuente". Si pusiéramos aquí la API Key, cualquiera podría copiarla y
 *  modificar el stock de TechStore.
 *
 *  Por eso: el usuario humano usa un Bearer Token (personal, con rol y que
 *  vence en minutos), y la API Key solo vive en servidores. Es justamente
 *  la lección de esta tarea.
 *
 *  Tampoco hay usuarios ni contraseñas escritos aquí: se ingresan en el formulario.
 * ============================================================
 */

// ---------- Configuración ----------

// URL del backend publicado en Render. ¡Cámbiala por la de tu servicio!
const URL_RENDER = "https://techstore-api-2znp.onrender.com";

// Si la página se abre en tu computadora, usa el backend local; si no, el de Render.
const ES_LOCAL = ["localhost", "127.0.0.1"].includes(location.hostname);
const API_URL = ES_LOCAL ? "http://localhost:8080" : URL_RENDER;

const REINTENTO_MS = 5000;          // reintentar cada 5 segundos...
const ESPERA_MAXIMA_MS = 120000;    // ...durante 2 minutos como máximo

// Clases del aviso de estado del servidor (formato compacto del encabezado)
const CLASES_ESTADO = "alert py-2 px-3 mb-0 small d-flex align-items-center gap-2 ";

// ---------- Estado de la página ----------

// El token se guarda SOLO en esta variable (memoria). No usamos localStorage:
// ahí cualquier script malicioso inyectado en la página podría leerlo.
// Contra: al recargar la página hay que volver a iniciar sesión.
let token = null;
let venceEn = null;         // momento (en milisegundos) en que vence el token
let temporizador = null;    // reloj que actualiza el tiempo restante

// Atajo para buscar elementos por id
const $ = (id) => document.getElementById(id);


// ============================================================
//  Función central: llamar a la API
// ============================================================
async function llamarApi(metodo, ruta, cuerpo = null, enviarToken = true) {
  const headers = {};
  if (cuerpo) headers["Content-Type"] = "application/json";

  // Así se envía un Bearer Token: en el header Authorization
  if (enviarToken && token) headers["Authorization"] = "Bearer " + token;

  mostrarPeticion(metodo, ruta, headers, cuerpo);

  const respuesta = await fetch(API_URL + ruta, {
    method: metodo,
    headers: headers,
    body: cuerpo ? JSON.stringify(cuerpo) : undefined
  });

  // Algunas respuestas pueden no traer JSON; en ese caso usamos un objeto vacío
  const datos = await respuesta.json().catch(() => ({}));
  return { status: respuesta.status, ok: respuesta.ok, datos: datos };
}

// Muestra qué se envió (el token se acorta para que se lea mejor)
function mostrarPeticion(metodo, ruta, headers, cuerpo) {
  let texto = metodo + " " + API_URL + ruta + "\n";

  for (const [nombre, valor] of Object.entries(headers)) {
    const visible = nombre === "Authorization"
      ? valor.slice(0, 27) + "…  (" + (valor.length - 7) + " caracteres)"
      : valor;
    texto += nombre + ": " + visible + "\n";
  }
  if (!headers["Authorization"] && metodo !== "GET" && ruta !== "/auth/login") {
    texto += "(sin header Authorization)\n";
  }

  if (cuerpo) {
    // Nunca mostramos la contraseña en pantalla
    const copia = { ...cuerpo };
    if ("password" in copia) copia.password = "********";
    texto += "\n" + JSON.stringify(copia, null, 2);
  }

  $("ultimaPeticion").textContent = texto;
}


// ============================================================
//  Alertas (verde = éxito, rojo = error, gris = enviando)
//  Se arman con textContent: los textos del servidor NUNCA se
//  interpretan como HTML (así evitamos ataques XSS).
// ============================================================
function alerta(tipo, titulo, detalle) {
  const div = document.createElement("div");
  div.className = "alert alert-" + tipo;
  div.setAttribute("role", "alert");

  const fuerte = document.createElement("strong");
  fuerte.textContent = titulo;
  div.append(fuerte, " · " + detalle);

  $("resultado").replaceChildren(div);
}

// Alerta roja a partir de un error de la API: { status, error, mensaje, ruta, timestamp }
function alertaDeError(r) {
  alerta("danger", r.status + " " + (r.datos.error || "Error"), r.datos.mensaje || "Error en la petición");
}

function alertaSinConexion() {
  alerta("danger", "Sin conexión", "No se pudo contactar al servidor (¿está encendido? ¿CORS?).");
}


// ============================================================
//  1) Despertar el servidor
// ============================================================
async function despertarServidor() {
  const inicio = Date.now();

  while (Date.now() - inicio < ESPERA_MAXIMA_MS) {
    try {
      // Cada intento espera como máximo 10 s una respuesta
      const r = await fetch(API_URL + "/api/public/estado", { signal: AbortSignal.timeout(10000) });
      if (r.ok) {
        servidorListo();
        return;
      }
    } catch {
      // Todavía dormido: reintentamos
    }

    const segundos = Math.round((Date.now() - inicio) / 1000);
    $("textoEspera").textContent = segundos + " s (puede tardar hasta 1 min)";
    await new Promise((listo) => setTimeout(listo, REINTENTO_MS));
  }

  // Pasaron 2 minutos sin respuesta
  const estado = $("estadoServidor");
  estado.className = CLASES_ESTADO + "alert-danger";
  const boton = document.createElement("button");
  boton.className = "btn btn-sm btn-light";
  boton.textContent = "Reintentar";
  boton.addEventListener("click", () => location.reload());
  estado.replaceChildren("El servidor no respondió en 2 minutos.", boton);
}

function servidorListo() {
  const estado = $("estadoServidor");
  estado.className = CLASES_ESTADO + "alert-success";
  estado.textContent = "✓ Servidor activo";
  document.querySelectorAll(".requiere-servidor").forEach((b) => (b.disabled = false));
  cargarCatalogo();
}


// ============================================================
//  2) Catálogo (GET público, no necesita token)
// ============================================================
async function cargarCatalogo() {
  try {
    const r = await fetch(API_URL + "/api/productos");
    const productos = await r.json();
    dibujarTabla(productos);
    llenarSelect(productos);
  } catch {
    alertaSinConexion();
  }
}

// Crea una celda. Si el valor es null (por ejemplo, tras el "caso de Ana" con PUT),
// muestra "null" en rojo.
function celda(valor, alinearDerecha = false, formato = (v) => v) {
  const td = document.createElement("td");
  if (alinearDerecha) td.className = "text-end";

  if (valor === null || valor === undefined) {
    const vacio = document.createElement("em");
    vacio.className = "text-danger";
    vacio.textContent = "null";
    td.append(vacio);
  } else {
    td.textContent = formato(valor);
  }
  return td;
}

function dibujarTabla(productos) {
  const filas = productos.map((p) => {
    const tr = document.createElement("tr");
    tr.append(
      celda(p.id),
      celda(p.nombre),
      celda(p.precio, true, (v) => "S/ " + Number(v).toFixed(2)),
      celda(p.stock, true)
    );
    return tr;
  });
  $("tablaProductos").replaceChildren(...filas);
}

function llenarSelect(productos) {
  const select = $("selectProducto");
  const elegido = select.value;   // conservar la opción elegida al recargar

  select.replaceChildren(...productos.map((p) => {
    const opcion = document.createElement("option");
    opcion.value = p.id;
    opcion.textContent = p.id + " · " + (p.nombre ?? "(sin nombre)");
    return opcion;
  }));

  if (elegido) select.value = elegido;
}


// ============================================================
//  3) Login: usuario + contraseña → Bearer Token
// ============================================================
async function iniciarSesion(evento) {
  evento.preventDefault();
  const cuerpo = { usuario: $("usuario").value.trim(), password: $("password").value };

  alerta("secondary", "Enviando", "Iniciando sesión…");
  try {
    const r = await llamarApi("POST", "/auth/login", cuerpo, false);
    if (!r.ok) {
      alertaDeError(r);   // 401: usuario o contraseña incorrectos
      return;
    }

    token = r.datos.accessToken;
    venceEn = Date.now() + r.datos.expiraEnSegundos * 1000;
    $("password").value = "";
    ocultarPassword();
    mostrarSesion(r.datos.usuario, r.datos.rol);
    alerta("success", r.status + " OK", "Bienvenido/a " + r.datos.usuario + ". Token " + r.datos.tokenType + " recibido.");
  } catch {
    alertaSinConexion();
  }
}

function mostrarSesion(usuario, rol) {
  $("sesionUsuario").textContent = usuario;
  $("sesionRol").textContent = rol;
  $("sesionRol").className = "badge " + (rol === "ADMIN" ? "text-bg-primary" : "text-bg-info");
  $("formLogin").classList.add("d-none");
  $("tarjetaSesion").classList.remove("d-none");

  clearInterval(temporizador);
  actualizarRestante();
  temporizador = setInterval(actualizarRestante, 1000);
}

// Cuenta regresiva. Al llegar a 0 NO borramos el token: así se puede ver el 401 "token vencido".
function actualizarRestante() {
  const ms = venceEn - Date.now();
  const texto = $("sesionRestante");

  if (ms <= 0) {
    texto.textContent = "VENCIDO (el servidor responderá 401)";
    texto.className = "text-danger";
    clearInterval(temporizador);
    return;
  }

  const minutos = Math.floor(ms / 60000);
  const segundos = Math.floor((ms % 60000) / 1000);
  texto.textContent = minutos + " min " + String(segundos).padStart(2, "0") + " s";
  texto.className = "";
}

// "Ojito": alterna entre ver y ocultar la contraseña.
// Solo cambia cómo se MUESTRA en pantalla; lo que se envía al servidor es lo mismo.
function alternarPassword() {
  const campo = $("password");
  const mostrar = campo.type === "password";

  campo.type = mostrar ? "text" : "password";
  $("iconoOjo").classList.toggle("d-none", mostrar);
  $("iconoOjoTachado").classList.toggle("d-none", !mostrar);

  const texto = mostrar ? "Ocultar contraseña" : "Mostrar contraseña";
  $("botonVerPassword").setAttribute("aria-label", texto);
  $("botonVerPassword").setAttribute("aria-pressed", String(mostrar));
  $("botonVerPassword").title = texto;
}

// Al iniciar sesión, la contraseña vuelve a quedar oculta (por si alguien mira la pantalla)
function ocultarPassword() {
  if ($("password").type === "text") alternarPassword();
}

function cerrarSesion() {
  token = null;
  venceEn = null;
  clearInterval(temporizador);
  $("tarjetaSesion").classList.add("d-none");
  $("formLogin").classList.remove("d-none");
  alerta("success", "Sesión cerrada", "El token se borró de la memoria de la página.");
}


// ============================================================
//  4) Actualizar precio: PATCH protegido (solo ADMIN)
//     PATCH = solo enviamos el campo que cambia: { "precio": ... }
// ============================================================
async function actualizarPrecio(evento) {
  evento.preventDefault();
  const id = $("selectProducto").value;
  const precio = parseFloat($("nuevoPrecio").value);
  const conToken = !$("sinToken").checked;

  alerta("secondary", "Enviando", "Petición PATCH en curso…");
  try {
    const r = await llamarApi("PATCH", "/api/productos/" + id, { precio: precio }, conToken);

    if (r.ok) {
      alerta("success", r.status + " OK",
        r.datos.nombre + ": nuevo precio S/ " + Number(r.datos.precio).toFixed(2));
      cargarCatalogo();
    } else {
      // 401 = sin token, token falso o vencido · 403 = token válido pero rol CLIENTE
      alertaDeError(r);
    }
  } catch {
    alertaSinConexion();
  }
}


// ============================================================
//  Arranque de la página
// ============================================================
$("textoApiUrl").textContent = "API: " + API_URL;
$("formLogin").addEventListener("submit", iniciarSesion);
$("formPrecio").addEventListener("submit", actualizarPrecio);
$("botonSalir").addEventListener("click", cerrarSesion);
$("botonVerPassword").addEventListener("click", alternarPassword);
$("botonRecargar").addEventListener("click", cargarCatalogo);

despertarServidor();
