package com.user.ncard.ui.group

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.user.ncard.repository.GroupRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.vo.Group
import com.user.ncard.vo.Resource
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class GroupViewModel @Inject constructor(val groupRepository: GroupRepository) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val groupList: LiveData<Resource<List<Group>>>

    init {
        groupList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Group>>>()
            } else {
                return@switchMap groupRepository.getALlGroups()
            }
        }
    }
}
