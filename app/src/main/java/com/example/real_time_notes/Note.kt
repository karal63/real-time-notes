package com.example.real_time_notes

data class Note(
    val id: String,
    val title: String,
    val description: String,
    val isPublic: Boolean,
    val ownerId: String
)
