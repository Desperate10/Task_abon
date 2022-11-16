package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.data.entities.CatalogEntity
import ua.POE.Task_abon.domain.model.Catalog


    fun mapCatalogEntityToCatalog(entity: CatalogEntity) = Catalog(
        id = entity.id,
        type = entity.type,
        code = entity.code,
        text = entity.text
    )
