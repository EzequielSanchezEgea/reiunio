// scripts.js - Funcionalidades básicas para la aplicación Reiunio

// Función para inicializar tooltips de Bootstrap
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar todos los tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    });

    // Cerrar automáticamente las alertas después de 5 segundos
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert.alert-success, .alert.alert-info');
        alerts.forEach(function(alert) {
            if (alert) {
                var bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        });
    }, 5000);

    // Manejar el filtrado de juegos/partidas/préstamos
    setupFilterForms();
});

// Configurar formularios de filtrado
function setupFilterForms() {
    var filterForms = document.querySelectorAll('.filter-form');
    
    filterForms.forEach(function(form) {
        var resetBtn = form.querySelector('.reset-filters');
        if (resetBtn) {
            resetBtn.addEventListener('click', function(e) {
                e.preventDefault();
                var inputs = form.querySelectorAll('input, select');
                inputs.forEach(function(input) {
                    if (input.type === 'checkbox') {
                        input.checked = false;
                    } else {
                        input.value = '';
                    }
                });
                form.submit();
            });
        }
    });
}

// Función para confirmar acciones importantes
function confirmarAccion(mensaje) {
    return confirm(mensaje || '¿Estás seguro de que deseas realizar esta acción?');
}

// Función para validar formulario de creación/edición de partida
function validarFormularioPartida() {
    var fechaPartida = document.getElementById('fecha').value;
    var horaInicio = document.getElementById('horaInicio').value;
    var horaFin = document.getElementById('horaFin').value;
    
    // Convertir a objetos Date para comparación
    var fechaActual = new Date();
    fechaActual.setHours(0, 0, 0, 0); // Restablecer a medianoche
    
    var fechaPartidaObj = new Date(fechaPartida);
    
    // Validar que la fecha no sea en el pasado
    if (fechaPartidaObj < fechaActual) {
        alert('La fecha de la partida no puede ser en el pasado.');
        return false;
    }
    
    // Validar que la hora de fin sea posterior a la de inicio
    if (horaInicio >= horaFin) {
        alert('La hora de fin debe ser posterior a la hora de inicio.');
        return false;
    }
    
    return true;
}

// Función para validar formulario de préstamo
function validarFormularioPrestamo() {
    var fechaDevolucion = document.getElementById('fechaDevolucionEstimada').value;
    var fechaActual = new Date();
    var fechaDevolucionObj = new Date(fechaDevolucion);
    
    // Validar que la fecha de devolución sea futura
    if (fechaDevolucionObj <= fechaActual) {
        alert('La fecha de devolución estimada debe ser posterior a hoy.');
        return false;
    }
    
    return true;
}

// Función para mostrar la cantidad de jugadores actuales en una partida
function actualizarContadorJugadores(partidaId, maxJugadores) {
    var contador = document.getElementById('contador-jugadores-' + partidaId);
    var botonUnirse = document.getElementById('boton-unirse-' + partidaId);
    
    if (contador && botonUnirse) {
        var cantidadActual = parseInt(contador.textContent.split('/')[0]);
        if (cantidadActual >= maxJugadores) {
            botonUnirse.disabled = true;
            botonUnirse.title = 'Esta partida está completa';
        }
    }
}