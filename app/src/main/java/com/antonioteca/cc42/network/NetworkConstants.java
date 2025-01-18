package com.antonioteca.cc42.network;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;

/**
 * Constantes que são usadas em toda a camada de rede, como URLs de
 * endpoints,
 * chaves de API,
 * tempos limite de conexão,
 * etc.
 */

public class NetworkConstants {

    public static final String BASE_URL = "https://api.intra.42.fr";
    public static final String UID = "u-s4t2ud-4ce9a69013fc7817425995ce488c2f0e9d4c968de61e0f7e51f4d5facc50cc27";
    public static final String SECRET = "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";

    public static String CODE = "code";
    public static String SCHEME_HOST = "cc42://checkcadet42";
    public static final int CAMERA_PERMISSION_CODE = 100;

    // Endpoints
    public static final String LOGIN_ENDPOINT = "/oauth/token";
    public static final String USER_INFO_ENDPOINT = "/v2/me";
    public static final String OAUTH_AUTHORIZE_ENDPOINT = "/oauth/authorize";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Outros valores relacionados à rede
    public static final int TIMEOUT_CONNECT = 15; // segundos
    public static final int TIMEOUT_READ = 30; // segundos

    // Códigos de status HTTP comuns
    public static final int HTTP_OK = 200;
    public static final int HTTP_UNAUTHORIZED = 401;
}
