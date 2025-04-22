// Funcionalidades específicas para la sección de juegos

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar funcionalidades para la sección de juegos
    initJuegosFilters();
    initDisponibilidadControl();
    validateJuegoForm();
});

// Función para inicializar los filtros de juegos
function initJuegosFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
    // Resetear todos los filtros
    const resetButton = filterForm.querySelector('.reset-filters');
    if (resetButton) {
        resetButton.addEventListener('click', function(e) {
            e.preventDefault();
            const inputs = filterForm.querySelectorAll('input, select');
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else {
                    input.value = '';
                }
            });
            filterForm.submit();
        });
    }
    
    // Auto-submit al cambiar un select
    const selectFilters = filterForm.querySelectorAll('select');
    selectFilters.forEach(select => {
        select.addEventListener('change', function() {
            filterForm.submit();
        });
    });
}

// Función para el control de disponibilidad de juegos
function initDisponibilidadControl() {
    const disponibilidadForms = document.querySelectorAll('form[action*="/disponibilidad"]');
    
    disponibilidadForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const action = form.querySelector('input[name="disponible"]').value === 'true' ? 
                'marcar como disponible' : 'marcar como no disponible';
            
            if (!confirm(`¿Estás seguro de que deseas ${action} este juego?`)) {
                e.preventDefault();
            }
        });
    });
}

// Función para validar el formulario de juegos
function validateJuegoForm() {
    const juegoForm = document.querySelector('form[action*="/juegos/"]');
    if (!juegoForm) return;
    
    juegoForm.addEventListener('submit', function(e) {
        let valid = true;
        
        // Validar que el mínimo de jugadores no sea mayor que el máximo
        const minJugadores = parseInt(document.getElementById('minJugadores').value);
        const maxJugadores = parseInt(document.getElementById('maxJugadores').value);
        
        if (minJugadores > maxJugadores) {
            alert('El número mínimo de jugadores no puede ser mayor que el máximo');
            valid = false;
        }
        
        // Validar que la duración sea positiva
        const duracion = parseInt(document.getElementById('duracionMinutos').value);
        if (duracion <= 0) {
            alert('La duración debe ser un número positivo');
            valid = false;
        }
        
        // Evitar envío del formulario si hay errores
        if (!valid) {
            e.preventDefault();
        }
    });
}

// Función para mostrar detalles de un juego en modal
function showJuegoDetails(juegoId) {
    // Aquí se podría implementar un modal que muestra rápidamente los detalles de un juego
    // mediante una solicitud AJAX sin necesidad de navegar a la página de detalles
    fetch(`/api/juegos/${juegoId}`)
        .then(response => response.json())
        .then(data => {
            // Actualizar el contenido del modal con los datos del juego
            const modal = document.getElementById('quickViewModal');
            if (modal) {
                modal.querySelector('.modal-title').textContent = data.nombre;
                modal.querySelector('.modal-body').innerHTML = `
                    <p><strong>Categoría:</strong> ${data.categoria}</p>
                    <p><strong>Jugadores:</strong> ${data.minJugadores}-${data.maxJugadores}</p>
                    <p><strong>Duración:</strong> ${data.duracionMinutos} minutos</p>
                    <p><strong>Estado:</strong> ${data.estado}</p>
                    <p><strong>Descripción:</strong> ${data.descripcion || 'No disponible'}</p>
                `;
                
                // Mostrar el modal
                const bsModal = new bootstrap.Modal(modal);
                bsModal.show();
            }
        })
        .catch(error => console.error('Error al cargar los detalles del juego:', error));
}

// Función para actualizar visualmente el estado de disponibilidad
function updateDisponibilidad(juegoId, disponible) {
    const badge = document.querySelector(`#disponibilidad-${juegoId}`);
    if (badge) {
        badge.textContent = disponible ? 'Disponible' : 'No disponible';
        badge.className = disponible ? 
            'badge bg-success availability-change' : 
            'badge bg-danger availability-change';
    }
}

// Función para filtrado rápido en la tabla de juegos
function quickFilter() {
    const input = document.getElementById('quickSearch');
    const filter = input.value.toUpperCase();
    const table = document.querySelector('.table-juegos');
    
    if (!table) return;
    
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const nameColumn = row.cells[0];
        const categoryColumn = row.cells[1];
        
        if (nameColumn && categoryColumn) {
            const nameText = nameColumn.textContent || nameColumn.innerText;
            const categoryText = categoryColumn.textContent || categoryColumn.innerText;
            
            if (nameText.toUpperCase().indexOf(filter) > -1 || 
                categoryText.toUpperCase().indexOf(filter) > -1) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });
}