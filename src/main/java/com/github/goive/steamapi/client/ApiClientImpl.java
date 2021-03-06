package com.github.goive.steamapi.client;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import com.github.goive.steamapi.enums.CountryCode;
import com.github.goive.steamapi.exceptions.SteamApiException;

/**
 * The client that connects to the Steam API to retrieve data.
 *
 * @author Ivan Antes-Klobucar
 */
public class ApiClientImpl implements ApiClient {

    private static final Logger logger = Logger.getLogger(ApiClientImpl.class);
    private static final String APP_ID_LIST_URL = "http://api.steampowered.com/ISteamApps/GetAppList/v0001/";

    private String apiUrl = "http://store.steampowered.com/api/appdetails?appids=";
    private ObjectMapper mapper = new ObjectMapper();
    private CountryCode countryCode;

    public ApiClientImpl() {
        countryCode = CountryCode.AT;
    }

    @Override
    public Map<Object, Object> retrieveResultBodyMap(long appId) throws SteamApiException {
        try {
            URL src = new URL(apiUrl + appId + "&cc=" + countryCode.name());
            return mapper.readValue(src, Map.class);
        } catch (IOException e) {
            throw new SteamApiException(e);
        }
    }

    @Override
    public Set<Long> retrieveValidAppIds() throws SteamApiException {
        Map<Object, Object> resultMap;
        try {
            URL src = new URL(APP_ID_LIST_URL);
            resultMap = mapper.readValue(src, Map.class);
        } catch (IOException e) {
            throw new SteamApiException("Error fetching list of valid AppIDs.", e);
        }

        Set<Long> validAppIds = extractAppIdsFromResult(resultMap);
        logger.debug(validAppIds.size() + " appIds returned from API");

        return validAppIds;
    }

    private Set<Long> extractAppIdsFromResult(Map<Object, Object> resultMap) {
        Map appMap = (Map) resultMap.get("applist");
        Map apps = (Map) appMap.get("apps");
        List appList = (List) apps.get("app");
        Set<Long> validAppIds = new HashSet<>();
        for (Object app : appList) {
            Map appItem = (Map) app;
            validAppIds.add(Long.parseLong(appItem.get("appid").toString()));
        }
        return validAppIds;
    }

    public void setCountryCode(CountryCode countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public CountryCode getCountryCode() {
        return countryCode;
    }

    public <M extends ObjectMapper> void setMapper(M mapper) {
        this.mapper = mapper;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
