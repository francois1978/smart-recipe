package smartrecipe.webgui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

@Route(value = "admin-utils")
public class AdminUtilsView extends VerticalLayout {
@Autowired
private HttpServletRequest req;

    AdminUtilsView() {
        UI.getCurrent().addBeforeEnterListener(new BeforeEnterListener() {
            @Override
            public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
                if (beforeEnterEvent.getNavigationTarget() != DeniedAccessView.class && // This is to avoid a
                        // loop if DeniedAccessView is the target
                        !req.isUserInRole("ADMIN")) {
                    beforeEnterEvent.rerouteTo(DeniedAccessView.class);
                }
            }
        });
    }
}