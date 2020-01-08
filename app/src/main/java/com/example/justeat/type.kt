package com.example.justeat

class type {

    var isSelected: Boolean = false
    var category: String? = null

    fun getCat(): String {
        return category.toString()
    }

    fun setCat(animal: String) {
        this.category = animal
    }

    fun getSelecteds(): Boolean {
        return isSelected
    }

    fun setSelecteds(selected: Boolean) {
        isSelected = selected
    }
}
