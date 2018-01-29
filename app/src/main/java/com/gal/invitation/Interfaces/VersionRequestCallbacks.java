package com.gal.invitation.Interfaces;

/**
 * Created on 01/07/2017.
 */

public interface VersionRequestCallbacks {

    void onSuccess(String currentVersion);
    void onError(String errorMessage);

}
