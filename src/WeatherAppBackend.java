import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherAppBackend {
    public static JSONObject getWeatherData(String locationName, int day, int hour, String tzid, String tempUnit) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=weather_code,temperature_2m,wind_speed_10m,relative_humidity_2m&timezone=" + tzid + tempUnit;

        try {
            HttpURLConnection connection = fetchAPIResponse(urlString);

            if (connection.getResponseCode() != 200) {
                System.out.println("Failed: HTTP error code " + connection.getResponseCode());
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultjsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONObject hourly = (JSONObject) resultjsonObject.get("hourly");
                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurerntTime(time, day, hour);

                JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
                double temperature = (double) temperatureData.get(index);

                JSONArray weatherCodeData = (JSONArray) hourly.get("weather_code");
                String weatherCondition = convertWeatherCode((long) weatherCodeData.get(index));

                JSONArray relativeHumidityData = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) relativeHumidityData.get(index);

                JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windSpeed = (double) windSpeedData.get(index);

                JSONObject weatherData = new JSONObject();
                weatherData.put("temperature", temperature);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("wind_speed", windSpeed);

                return weatherData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection connection = fetchAPIResponse(urlString);

            if(connection.getResponseCode() != 200) {
                System.out.println("Failed: HTTP error code " + connection.getResponseCode());
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultJsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONArray locationData = (JSONArray) resultJsonObject.get("results");
                return locationData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getSunsetSunriseData(String locationName, String tzid) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.sunrise-sunset.org/json?lat=" + latitude +
                "&lng=" + longitude + "&formatted=0" + "&tzid=" + tzid;

        try {
            HttpURLConnection connection = fetchAPIResponse(urlString);

            if(connection.getResponseCode() != 200) {
                System.out.println("Failed: HTTP error code " + connection.getResponseCode());
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultJsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONObject results = (JSONObject) resultJsonObject.get("results");
                return results;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getTimezoneData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.wheretheiss.at/v1/coordinates/" + latitude + "," + longitude;

        try {
            HttpURLConnection connection = fetchAPIResponse(urlString);

            if(connection.getResponseCode() != 200) {
                System.out.println("Failed: HTTP error code " + connection.getResponseCode());
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultJsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

                return resultJsonObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchAPIResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int findIndexOfCurerntTime(JSONArray timeList, int day, int hour) {
        String currentTime = getCurrentTime(day, hour);

        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }
        return 0;
    }

    private static String getCurrentTime(int day, int hour) {
        LocalDateTime currentDateTime = LocalDateTime.now()
                .plusDays(day)
                .withHour(hour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = "";
        if (weatherCode == 0L) {
            weatherCondition = "Clear";
        } else if (weatherCode <= 3L && weatherCode >= 1L) {
            weatherCondition = "Partly Cloudy";
        } else if (weatherCode <= 48L && weatherCode >= 45L) {
            weatherCondition = "Fog";
        } else if (weatherCode <= 55L && weatherCode >= 51L) {
            weatherCondition = "Drizzle";
        } else if (weatherCode <= 57L && weatherCode >= 56L) {
            weatherCondition = "Freezing Drizzle";
        } else if (weatherCode <= 65L && weatherCode >= 61L) {
            weatherCondition = "Rain";
        } else if (weatherCode <= 67L && weatherCode >= 66L) {
            weatherCondition = "Freezing Rain";
        } else if (weatherCode <= 75L && weatherCode >= 71L) {
            weatherCondition = "Snow Fall";
        } else if (weatherCode == 77L) {
            weatherCondition = "Snow Grains";
        } else if (weatherCode <= 82L && weatherCode >= 80L) {
            weatherCondition = "Rain Showers";
        } else if (weatherCode <= 86L && weatherCode >= 85L) {
            weatherCondition = "Snow Showers";
        } else if (weatherCode == 95L) {
            weatherCondition = "Thunderstorm";
        } else if (weatherCode <= 99L && weatherCode >= 96L) {
            weatherCondition = "Thunderstorm with Hail";
        }
        return weatherCondition;
    }
}
