package smartrecipe.webgui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import lombok.extern.slf4j.Slf4j;

//@Push
@Slf4j
@Route("push")
public class PushView extends VerticalLayout {
    private Label message = new Label("Initial state");
    private Button runFeeder = new Button("Run feeder");

    public PushView() {
        add(runFeeder,message);
        runFeeder.addClickListener(e->initFeeder());

    }

    public void initFeeder() {
        new DjTrackFeeder().start();
    }

    class DjTrackFeeder extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                // Update the data for a while
                while (count < 100) {
                    Thread.sleep(1000);
                    getUI().get().access((Command) () -> message.setText("Count :" + count));
                    getUI().get().push();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
