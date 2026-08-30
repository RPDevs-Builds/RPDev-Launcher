package iamrp.dev.launcher.iconpack

import iamrp.dev.launcher.data.models.IconPickerItem

data class IconPickerCategory(
    val title: String,
    val items: List<IconPickerItem>
)

fun IconPickerCategory.filter(searchQuery: String): IconPickerCategory {
    return IconPickerCategory(
        title = title,
        items = items
            .filter { it.label.lowercase().contains(searchQuery.lowercase()) }
    )
}
