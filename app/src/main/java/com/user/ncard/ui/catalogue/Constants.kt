/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.user.ncard.ui.catalogue

import com.user.ncard.util.AppHelper

open class Constants {

    companion object {
        /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
        @JvmField var COGNITO_POOL_ID = AppHelper.getCognitoPoolId()

        /*
         * Region of your Cognito identity pool ID.
         */
        @JvmField var COGNITO_POOL_REGION = AppHelper.getCognitoPoolRegion()

        /*
         * Note, you must first create a bucket using the S3 console before running
         * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
         * put it's name in the field below.
         */
        @JvmField var BUCKET_NAME = AppHelper.getCognitoBucketName() + "/"

        @JvmField var PATH_NCARD = "https://s3-ap-northeast-1.amazonaws.com/" + BUCKET_NAME
        const val FOLDER_CATALOGUE = "catalogue"
        const val FOLDER_CHAT = "chat"

        /*
         * Region of your bucket.
         */
        const val BUCKET_REGION = AppHelper.BUCKET_REGION

        const val SINGAPORE_TIMEZONE = "Asia/Singapore"

        const val FOLDER_NAME_TEMP = "CardlineTemp"

    }


}
