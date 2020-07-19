package smartrecipe.webgui.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;


//@Push

@Route("push")
@Push
@Slf4j
public class PushView extends VerticalLayout {
    private Label message = new Label("Initial state");
    private Button runFeeder = new Button("Run feeder");

    public PushView() {
        add(runFeeder, message);
        runFeeder.addClickListener(e -> initFeeder());

    }

    public Label getMessage() {
        return message;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        add(new Span("Waiting for updates"));

        // Start the data feed thread
        new DjTrackFeeder(attachEvent.getUI(), this).start();
    }

    public void initFeeder() {

    }

    class DjTrackFeeder extends Thread {

        private UI ui;
        private PushView view;

        public DjTrackFeeder(UI ui, PushView view) {
            this.ui = ui;
            this.view = view;
        }

        int count = 0;

        @Override
        public void run() {
            try {
                // Update the data for a while
                while (count < 100) {
                    Thread.sleep(1000);
                    count++;
                    ui.access(() -> view.getMessage().setText("Count :" + count));
                    log.info("Log pushed on GUI " + count);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
