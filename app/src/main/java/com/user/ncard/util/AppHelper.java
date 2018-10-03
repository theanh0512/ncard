/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.user.ncard.util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.model.AttributeType;
import com.user.ncard.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppHelper {
    // App settings

    private static List<String> attributeDisplaySeq;
    private static Map<String, String> signUpFieldsC2O;
    private static Map<String, String> signUpFieldsO2C;

    private static AppHelper appHelper;
    private static CognitoUserPool userPool;
    private static String user;
    private static String userEmail;
    private static String userId;
    private static CognitoDevice newDevice;

    private static CognitoUserAttributes attributesChanged;
    private static List<AttributeType> attributesToDelete;

    private static  int itemCount;
    private static int trustedDevicesCount;
    private static List<CognitoDevice> deviceDetails;
    private static CognitoDevice thisDevice;
    private static boolean thisDeviceTrustState;
    private static Map<String, String> firstTimeLogInUserAttributes;
    private static List<String> firstTimeLogInRequiredAttributes;
    private static int firstTimeLogInItemsCount;
    private static Map<String, String> firstTimeLogInUpDatedAttributes;
    private static String firstTimeLoginNewPassword;

    // Change the next three lines of code to run this demo on your userList pool

    public static String getCognitoPoolId() {
        String COGNITO_POOL_ID = "ap-northeast-1:44f110e1-bd86-4eef-a03c-e6f962576ff9";
        if(BuildConfig.DEBUG && Constants.DEV) {
            COGNITO_POOL_ID = "ap-northeast-1:44f110e1-bd86-4eef-a03c-e6f962576ff9";
        } else {
            COGNITO_POOL_ID = "ap-northeast-1:9e1d05cc-2bec-4be5-89f3-e3e15913fcfb";
        }
        return COGNITO_POOL_ID;
    }

    /*
     * Region of your Cognito identity pool ID.
     */
    public static String getCognitoPoolRegion() {
        String COGNITO_POOL_REGION = "ap-northeast-1";
        if(BuildConfig.DEBUG && Constants.DEV) {
            COGNITO_POOL_REGION = "ap-northeast-1";
        } else {
            COGNITO_POOL_REGION = "ap-northeast-1";
        }
        return COGNITO_POOL_REGION;
    }

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static String getCognitoBucketName() {
        String BUCKET_NAME = "ncard-assets";
        if(BuildConfig.DEBUG && Constants.DEV) {
            BUCKET_NAME = "ncard-assets";
        } else {
            BUCKET_NAME = "ncard-assets-production";
        }
        return BUCKET_NAME;
    }

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "ap-northeast-1";

    /**
     * Add your pool id here
     */
    public static String getCognitoUserPoolId() {
        String userPoolId = "ap-northeast-1_L4yJBxENq";
        if(BuildConfig.DEBUG && Constants.DEV) {
            userPoolId = "ap-northeast-1_L4yJBxENq";
        } else {
            userPoolId = "ap-northeast-1_etqF4vXAK";
        }
        return userPoolId;
    }

    /**
     * Add you app id
     */
    public static String getCognitoClientId() {
        String clientId = "5neehs3amosr0qke8jvvjt2hur";
        if(BuildConfig.DEBUG && Constants.DEV) {
            clientId = "5neehs3amosr0qke8jvvjt2hur";
        } else {
            clientId = "4p3a3inh7oiv4b8kri2ekd1d86";
        }
        return clientId;
    }

    /**
     * App secret associated with your app id - if the App id does not have an associated App secret,
     * set the App secret to null.
     * e.g. clientSecret = null;
     */
    public static String getCognitoClientSecret() {
        String clientSecret = "1hf02sn8t0889o0a4u9d3m91odjofkd5d5t1ngn83eqrasrrntf";
        if(BuildConfig.DEBUG && Constants.DEV) {
            clientSecret = "1hf02sn8t0889o0a4u9d3m91odjofkd5d5t1ngn83eqrasrrntf";
        } else {
            clientSecret = "saqlj3dlm31qjq0mp9ncbh5lros119vauovthchko1lrfj2cvfq";
        }
        return clientSecret;
    }

    /**
     * Set Your User Pools region.
     * e.g. if your user pools are in US East (N Virginia) then set cognitoRegion = Regions.US_EAST_1.
     */
    private static final Regions cognitoRegion = Regions.AP_NORTHEAST_1;

    // User details from the service
    private static CognitoUserSession currSession;
    private static CognitoUserDetails userDetails;

    // User details to display - they are the current values, including any local modification
    private static boolean phoneVerified;
    private static boolean emailVerified;

    private static boolean phoneAvailable;
    private static boolean emailAvailable;

    private static Set<String> currUserAttributes;

    public static void init(Context context) {
        setData();

        if (appHelper != null && userPool != null) {
            return;
        }

        if (appHelper == null) {
            appHelper = new AppHelper();
        }

        if (userPool == null) {

            // Create a user pool with default ClientConfiguration
            userPool = new CognitoUserPool(context, getCognitoUserPoolId(), getCognitoClientId(), getCognitoClientSecret(), cognitoRegion);
        }

        phoneVerified = false;
        phoneAvailable = false;
        emailVerified = false;
        emailAvailable = false;

        newDevice = null;
        thisDevice = null;
        thisDeviceTrustState = false;
    }

    public static CognitoUserPool getPool() {
        return userPool;
    }

    public static Map<String, String> getSignUpFieldsC2O() {
        return signUpFieldsC2O;
    }

    public static Map<String, String> getSignUpFieldsO2C() {
        return signUpFieldsO2C;
    }

    public static List<String> getAttributeDisplaySeq() {
        return attributeDisplaySeq;
    }

    public static void setCurrSession(CognitoUserSession session) {
        currSession = session;
    }

    public static CognitoUserSession getCurrSession() {
        return currSession;
    }

    public static void setUserDetails(CognitoUserDetails details) {
        userDetails = details;
    }

    public static CognitoUserDetails getUserDetails() {
        return userDetails;
    }

    public static String getCurrUser() {
        return user;
    }

    public static void setUser(String newUser) {
        user = newUser;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        AppHelper.userEmail = userEmail;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        AppHelper.userId = userId;
    }

    public static List<String> getNewAvailableOptions() {
        List<String> newOption = new ArrayList<String>();
        for(String attribute : attributeDisplaySeq) {
            if(!(currUserAttributes.contains(attribute))) {
                newOption.add(attribute);
            }
        }
        return  newOption;
    }
    public static CognitoDevice getDeviceDetail(int position) {
        if (position <= trustedDevicesCount) {
            return deviceDetails.get(position);
        } else {
            return null;
        }
    }

    public static String formatException(Exception exception) {
        String formattedString = "Internal Error";
        Log.e("App Error",exception.toString());
        Log.getStackTraceString(exception);

        String temp = exception.getMessage();

        if(temp != null && temp.length() > 0) {
            formattedString = temp.split("\\(")[0];
            if(temp != null && temp.length() > 0) {
                return formattedString;
            }
        }

        return  formattedString;
    }

    private static void setData() {
        // Set attribute display sequence
        attributeDisplaySeq = new ArrayList<String>();
        attributeDisplaySeq.add("given_name");
        attributeDisplaySeq.add("middle_name");
        attributeDisplaySeq.add("family_name");
        attributeDisplaySeq.add("nickname");
        attributeDisplaySeq.add("phone_number");
        attributeDisplaySeq.add("email");

        signUpFieldsC2O = new HashMap<String, String>();
        signUpFieldsC2O.put("Email verified", "email_verified");
        signUpFieldsC2O.put("Email","email");

        signUpFieldsO2C = new HashMap<String, String>();
        signUpFieldsO2C.put("email_verified", "Email verified");
        signUpFieldsO2C.put("email", "Email");

    }

    public static void newDevice(CognitoDevice device) {
        newDevice = device;
    }


    private static void modifyAttribute(String attributeName, String newValue) {
        //

    }

    private static void deleteAttribute(String attributeName) {

    }
}

