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
import java.util.Locale;

public class WeatherAppGUI extends JFrame {
    private JSONObject weatherData;

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

        JToggleButton timezoneToggle = new JToggleButton("auto");
        timezoneToggle.setBounds(145, 70, 100, 30);
        timezoneToggle.setFont(new Font("Dialog", Font.PLAIN, 16));
        timezoneToggle.addActionListener(e -> {
            if (timezoneToggle.isSelected()) {
                timezoneToggle.setText("Tbilisi");
            } else {
                timezoneToggle.setText("Auto");
            }
        });
        add(timezoneToggle);

        JLabel instructionLabel = new JLabel("<html><b>Select hour and toggle timezone(Auto or Tbilisi).</b> " +
                "Auto timezone equates time in your location with the time in the searched city.</html>");
        instructionLabel.setBounds(260, 70, 320, 30);
        instructionLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        add(instructionLabel);

        JLabel cityLabel = new JLabel("...");
        cityLabel.setBounds(0, 120, 565, 45);
        cityLabel.setFont(new Font("Dialog", Font.PLAIN, 25));
        cityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cityLabel);

        JLabel weatherConditionImage = new JLabel(loadImage("src/pics/cloudy.png"));
        weatherConditionImage.setBounds(60, 200, 450, 225);
        add(weatherConditionImage);

        JLabel temperatureText = new JLabel("Enter the city");
        temperatureText.setBounds(60, 420, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDescription = new JLabel("...");
        weatherConditionDescription.setBounds(60, 480, 450, 36);
        weatherConditionDescription.setFont(new Font("Dialog", Font.PLAIN, 35));
        weatherConditionDescription.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDescription);

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

                String selectedHour = (String) hourSelector.getSelectedItem();
                int hour = selectedHour.equals("now") ?
                        LocalTime.now().getHour() :
                        Integer.parseInt(selectedHour.split(":")[0]);

                weatherData = WeatherAppBackend.getWeatherData(userInput, hour, timezoneToggle.getText().toLowerCase());

                String weatherCondition = (String) weatherData.get("weather_condition");

                int currentHour = LocalTime.now().withHour(hour).getHour();

                switch (weatherCondition) {
                    case "Clear":
                        if ((currentHour >= 19 && currentHour <= 23) || (currentHour >= 0 && currentHour <= 7)) {
                            weatherConditionImage.setIcon(loadImage("src/pics/clear_nighttime.png"));
                        } else {
                            weatherConditionImage.setIcon(loadImage("src/pics/clear.png"));
                        }
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/pics/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/pics/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/pics/snow.png"));
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
