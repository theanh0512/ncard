package com.user.ncard.ui.catalogue.tag

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.user.ncard.repository.CatalogueRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.vo.Resource
import com.user.ncard.vo.TagGroupPost
import com.user.ncard.vo.TagPost
import javax.inject.Inject

/**
 * Created by trong-android-dev on 20/10/17.
 */
class TagPostViewModel @Inject constructor(val catalogueRepository: CatalogueRepository) : BaseViewModel() {

    val start = MutableLiveData<Boolean>()
    lateinit var tags: LiveData<Resource<List<TagPost>>>
    lateinit var tagGroups: LiveData<List<TagGroupPost>>

    var tagsSelected: List<String>? = null

    init {
        initData()
    }

    fun init(tagsSelected: List<String>?) {
        this.tagsSelected = tagsSelected
    }

    override fun initData() {
        tags = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<TagPost>>>()
            }
            return@switchMap catalogueRepository.getCatalogueTags()
        }

        tagGroups = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<TagGroupPost>>()
            }
            return@switchMap catalogueRepository.getCatalogueTagGroups()
        }
    }

    fun initTagsSelected() {
        if (tagsSelected != null) {
            tags?.value?.data?.forEach { tag ->
                tagsSelected?.forEach { tagsSelected ->
                    if (tag.id.toString() == tagsSelected) {
                        tag.selected = true
                    }
                }
            }
        }
    }

}