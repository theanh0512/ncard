package com.user.ncard.ui.catalogue

import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.vo.CataloguePost

/**
 * Created by trong-android-dev on 16/10/17.
 */
data class CataloguePostResponse(var posts: List<CataloguePost>, var pagination: Pagination)