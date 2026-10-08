package com.vozbarrial.domain.enums

enum class UserRole(val value: String) {
    USUARIO("USUARIO"),
    ADMIN("ADMIN");

    companion object {
        fun fromValue(value: String): UserRole {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: USUARIO
        }
    }
}