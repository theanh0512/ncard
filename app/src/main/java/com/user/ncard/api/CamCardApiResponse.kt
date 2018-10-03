package com.user.ncard.api

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 23/9/2017.
 */
data class CamCardApiResponse(@SerializedName("email") val emails: List<Email>,
                              @SerializedName("formatted_name") val names: List<Name>,
                              @SerializedName("label") val labels: List<LabelWrapper>,
                              @SerializedName("organization") val organizations: List<OrganizationWrapper>,
                              @SerializedName("telephone") val telephones: List<TelephoneWrapper>,
                              @SerializedName("title") val titles: List<Title>)

data class Email(@SerializedName("item") val item: String)

data class Name(@SerializedName("item") val item: String)

data class LabelWrapper(val item:Label)
data class Label(@SerializedName("address") val address: String)

data class OrganizationWrapper(val item: Organization)
data class Organization(val name: String)

data class TelephoneWrapper(val item: Telephone)
data class Telephone(val number: String)

data class Title(val item: String)