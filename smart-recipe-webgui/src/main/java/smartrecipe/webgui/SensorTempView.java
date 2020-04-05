package smartrecipe.webgui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Route("sensor")
public class SensorTempView extends VerticalLayout {

    private Grid<SensorDataDto> grid = null;

    //labels
    private Label maxTempLabel = new Label("Max temperature");
    private Label minTempLabel = new Label("Min temperature");

    private Label maxTempValueLabel = new Label("");
    private Label minTempValueLabel = new Label("");

    private DatePicker datePicker = new DatePicker("Snapshot date");
    private final Button loadDataButton = new Button("Load data", VaadinIcon.DOWNLOAD.create());

    @Value("${service.url}")
    private String serviceUrl;

    public SensorTempView() {

        this.grid = new Grid<>(SensorDataDto.class);
        HorizontalLayout maxMinLayout = new HorizontalLayout();
        maxMinLayout.add(minTempLabel, minTempValueLabel, maxTempLabel, maxTempValueLabel);
        add(datePicker, loadDataButton, maxMinLayout, grid);
        loadDataButton.addClickListener(e -> loadData());
    }

    private void loadData() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = datePicker.getValue().format(formatter);

        //call api
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SensorDataDto[]> response =
                restTemplate.getForEntity(serviceUrl +
                        "/sensordata/getdata/" +
                        "TEMPERATURE/" +
                        formattedDate + "/" + formattedDate, SensorDataDto[].class);

        List<SensorDataDto> data = Arrays.asList(response.getBody());
        log.info("Number of total sensor data loaded: " + data.size());

        if(CollectionUtils.isEmpty(data)){
            return;
        }

        //get max / min temp
        Comparator<SensorDataDto> comparator = Comparator.comparing(SensorDataDto::getValueNumeric);
        SensorDataDto minTemp = Arrays.asList(response.getBody()).stream().min(comparator).get();
        SensorDataDto maxTemp = Arrays.asList(response.getBody()).stream().max(comparator).get();
        minTempValueLabel.setText("" + minTemp.getValueNumeric());
        maxTempValueLabel.setText("" + maxTemp.getValueNumeric());

        //set grid items
        grid.setItems(data);

    }

}
