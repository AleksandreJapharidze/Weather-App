import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class WeatherAppGUI extends JFrame {
    private JSONObject weatherData;
    private JLabel sunrisesunsetIndicator;

    public WeatherAppGUI() {
        super("Simple Weather App");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        addGUIComponents();
    }

    private void addGUIComponents() {
        JTextField searchField = new JTextField();
        searchField.setBounds(15, 15, 500, 45);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchField);

        String[] hours = new String[25];
        hours[0] = "now";
        for (int i = 0; i < 24; i++) {
            hours[i + 1] = String.format("%02d:00", i);
        }

        JComboBox<String> hourSelector = new JComboBox<>(hours);
        hourSelector.setBounds(15, 70, 120, 30);
        hourSelector.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(hourSelector);

        String[] weatherDays = {"Today", "Tomorrow", "After tomorrow", "In 3 days", "In 4 days", "In 5 days", "In 6 days"};
        JComboBox<String> daySelector = new JComboBox<>(weatherDays);
        daySelector.setBounds(15, 110, 120, 30);
        daySelector.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(daySelector);

        JLabel cityLabel = new JLabel("...");
        cityLabel.setBounds(0, 120, 565, 45);
        cityLabel.setFont(new Font("Dialog", Font.PLAIN, 25));
        cityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cityLabel);

        JLabel weatherConditionImage = new JLabel(loadImage("src/batch/cloudy.png"));
        weatherConditionImage.setBounds(60, 200, 450, 225);
        add(weatherConditionImage);

        JLabel temperatureText = new JLabel("Enter the city");
        temperatureText.setBounds(60, 420, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDescription = new JLabel("...");
        weatherConditionDescription.setBounds(60, 480, 450, 45);
        weatherConditionDescription.setFont(new Font("Dialog", Font.PLAIN, 35));
        weatherConditionDescription.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDescription);

        sunrisesunsetIndicator = new JLabel();
        sunrisesunsetIndicator.setBounds(450, 100, 100, 100);
        add(sunrisesunsetIndicator);

        JLabel humidityImage =  new JLabel(loadImage("src/pics/humidity.png"));
        humidityImage.setBounds(25, 650, 74, 66);
        add(humidityImage);

        JLabel humidityText = new JLabel("...");
        humidityText.setBounds(100, 655, 85, 66);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 15));
        add(humidityText);

        JLabel windSpeedImage = new JLabel(loadImage("src/pics/windspeed.png"));
        windSpeedImage.setBounds(370, 650, 74, 66);
        add(windSpeedImage);

        JLabel windSpeedText = new JLabel("...");
        windSpeedText.setBounds(455, 660, 85, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 15));
        add(windSpeedText);

        JButton searchButton = new JButton(loadImage("src/pics/search.png"));
        searchButton.setBounds(520, 15, 50, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchField.getText();
                JSONArray locationData = WeatherAppBackend.getLocationData(userInput);

                if (locationData != null) {
                    cityLabel.setText(userInput);
                }

                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                String selectedDay = (String) daySelector.getSelectedItem();
                int dayOffset = 0;
                switch (selectedDay) {
                    case "Today" -> dayOffset = 0;
                    case "Tomorrow" -> dayOffset = 1;
                    case "After tomorrow" -> dayOffset = 2;
                    case "In 3 days" -> dayOffset = 3;
                    case "In 4 days" -> dayOffset = 4;
                    case "In 5 days" -> dayOffset = 5;
                    case "In 6 days" -> dayOffset = 6;
                }

                String selectedHour = (String) hourSelector.getSelectedItem();
                int hour = selectedHour.equals("now") ?
                        LocalTime.now().getHour() :
                        Integer.parseInt(selectedHour.split(":")[0]);

                weatherData = WeatherAppBackend.getWeatherData(userInput, dayOffset, hour);

                String weatherCondition = (String) weatherData.get("weather_condition");

                int currentHour = LocalTime.now().withHour(hour).getHour();

                JSONObject results = WeatherAppBackend.getSunsetSunriseData(userInput);
                if (results == null) {
                    return;
                }

                String sunrise = (String) results.get("sunrise");
                String sunset = (String) results.get("sunset");

                OffsetDateTime currentTime = OffsetDateTime.now().withHour(currentHour);
                OffsetDateTime sunriseTime = OffsetDateTime.parse(sunrise);
                OffsetDateTime sunsetTime = OffsetDateTime.parse(sunset);

                int sunriseHour = sunriseTime.getHour();
                int sunsetHour = sunsetTime.getHour();

                if (sunriseHour == currentHour) {
                    sunrisesunsetIndicator.setIcon(loadImage("src/batch/sunrise.png"));
                } else if (sunsetHour == currentHour) {
                    sunrisesunsetIndicator.setIcon(loadImage("src/batch/sunset.png"));
                } else {
                    sunrisesunsetIndicator.setIcon(null);
                }

                int currentHourMinute = currentTime.getHour() * 100 + currentTime.getMinute();
                int sunriseHourMinute = sunriseTime.getHour() * 100 + sunriseTime.getMinute();
                int sunsetHourMinute = sunsetTime.getHour() * 100 + sunsetTime.getMinute();

                boolean isNightTime;

                if (sunriseHourMinute <= sunsetHourMinute) {
                    // Normal case: sunrise before sunset in the same day
                    // Night time is before sunrise OR after sunset
                    isNightTime = currentHourMinute < sunriseHourMinute || currentHourMinute > sunsetHourMinute;
                } else {
                    // Wrapped case: sunset time appears "earlier" than sunrise due to timezone conversion
                    // This means sunset is actually the next day in the original timezone
                    // Night time is after sunset AND before sunrise
                    isNightTime = currentHourMinute > sunsetHourMinute && currentHourMinute < sunriseHourMinute;
                }

                System.out.println("Current time: " + currentTime);
                System.out.println("Sunrise time: " + sunriseTime);
                System.out.println("Sunset time: " + sunsetTime);

                System.out.println("Current hour (24h): " + currentTime.getHour());
                System.out.println("Sunrise hour (24h): " + sunriseTime.getHour());
                System.out.println("Sunset hour (24h): " + sunsetTime.getHour());
                System.out.println("Is night time: " + isNightTime);

                switch (weatherCondition) {
                    case "Clear":
                        if (isNightTime) {
                            weatherConditionImage.setIcon(loadImage("src/batch/clear-night.png"));
                        } else {
                            weatherConditionImage.setIcon(loadImage("src/batch/clear-day.png"));
                        }
                        break;
                    case "Partly Cloudy":
                        if (isNightTime) {
                            weatherConditionImage.setIcon(loadImage("src/batch/partly-cloudy-night.png"));
                        } else {
                            weatherConditionImage.setIcon(loadImage("src/batch/partly-cloudy-day.png"));
                        }
                        break;
                    case "Fog":
                        weatherConditionImage.setIcon(loadImage("src/batch/fog.png"));
                        break;
                    case "Drizzle", "Freezing Drizzle", "Rain Showers":
                        if (isNightTime) {
                            weatherConditionImage.setIcon(loadImage("src/batch/showers-night.png"));
                        } else {
                            weatherConditionImage.setIcon(loadImage("src/batch/showers-day.png"));
                        }
                        break;
                    case "Rain", "Freezing Rain":
                        weatherConditionImage.setIcon(loadImage("src/batch/rain.png"));
                        break;
                    case "Snow Fall", "Snow Grains":
                        weatherConditionImage.setIcon(loadImage("src/batch/snow.png"));
                        break;
                    case "Snow Showers":
                        if (isNightTime) {
                            weatherConditionImage.setIcon(loadImage("src/batch/snow-showers-night.png"));
                        } else {
                            weatherConditionImage.setIcon(loadImage("src/batch/snow-showers-day.png"));
                        }
                        break;
                    case "Thunderstorm":
                        weatherConditionImage.setIcon(loadImage("src/batch/thunder.png"));
                        break;
                    case "Thunderstorm with Hail":
                        weatherConditionImage.setIcon(loadImage("src/batch/hail.png"));
                        break;
                }

                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                weatherConditionDescription.setText(weatherCondition);

                long  humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> "  + humidity + "%</html>");

                double windSpeed = (double) weatherData.get("wind_speed");
                windSpeedText.setText("<html><b>Wind speed</b> " + windSpeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    private ImageIcon loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new ImageIcon(image);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Path not found");
        return null;
    }
}
