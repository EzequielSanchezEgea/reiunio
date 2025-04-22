package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellidos;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToMany(mappedBy = "usuario")
    private List<Prestamo> prestamos = new ArrayList<>();

    @OneToMany(mappedBy = "creador")
    private List<Partida> partidasCreadas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<JugadorPartida> participaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<HistorialCambios> cambios = new ArrayList<>();

    // Constructor vacío necesario para JPA
    public Usuario() {
    }

    // Constructor con campos básicos
    public Usuario(String username, String password, String email, String nombre, String apellidos, Rol rol) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.fechaRegistro = LocalDate.now();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public List<Partida> getPartidasCreadas() {
        return partidasCreadas;
    }

    public void setPartidasCreadas(List<Partida> partidasCreadas) {
        this.partidasCreadas = partidasCreadas;
    }

    public List<JugadorPartida> getParticipaciones() {
        return participaciones;
    }

    public void setParticipaciones(List<JugadorPartida> participaciones) {
        this.participaciones = participaciones;
    }

    public List<HistorialCambios> getCambios() {
        return cambios;
    }

    public void setCambios(List<HistorialCambios> cambios) {
        this.cambios = cambios;
    }

    @Override
    public String toString() {
        return "Usuario [id=" + id + ", username=" + username + ", email=" + email + ", nombre=" + nombre
                + ", apellidos=" + apellidos + ", rol=" + rol + "]";
    }
}