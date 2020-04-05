package smartrecipe.webgui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Route(value = "login")
public class LoginView
        extends FlexLayout implements BeforeEnterObserver {

    private final Label label;
    private final TextField userNameTextField;
    private final PasswordField passwordField;

    /**
    * AuthenticationManager is already exposed in WebSecurityConfig
    */
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private HttpServletRequest req;

    LoginView() {
        label = new Label("Please login...");

        userNameTextField = new TextField();
        userNameTextField.setPlaceholder("Username");
        //UiUtils.makeFirstInputTextAutoFocus(Collections.singletonList(userNameTextField));

        passwordField = new PasswordField();
        passwordField.setPlaceholder("Password");
        passwordField.addKeyDownListener(Key.ENTER, (ComponentEventListener<KeyDownEvent>) keyDownEvent -> authenticateAndNavigate());

        Button submitButton = new Button("Login");
        submitButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            authenticateAndNavigate();
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(label, userNameTextField, passwordField, submitButton);
        add(formLayout);

        // center the form
        setAlignItems(Alignment.CENTER);
        this.getElement().getStyle().set("height", "100%");
        this.getElement().getStyle().set("justify-content", "center");
    }

    private void    authenticateAndNavigate() {
        /*
        Set an authenticated user in Spring Security and Spring MVC
        spring-security
        */
        UsernamePasswordAuthenticationToken authReq
                = new UsernamePasswordAuthenticationToken(userNameTextField.getValue(), passwordField.getValue());
        try {
            // Set authentication
            Authentication auth = authManager.authenticate(authReq);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            /*
            Navigate to the requested page:
            This is to redirect a user back to the originally requested URL – after they log in as we are not using
            Spring's AuthenticationSuccessHandler.
            */
            HttpSession session = req.getSession(false);
            DefaultSavedRequest savedRequest = (DefaultSavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            String requestedURI = savedRequest != null ? savedRequest.getRequestURI() : "/main";

            this.getUI().ifPresent(ui -> ui.navigate(StringUtils.removeStart(requestedURI, "/")));
        } catch (BadCredentialsException e) {
            label.setText("Invalid username or password. Please try again.");
        }
    }

    /**
    * This is to redirect user to the main URL context if (s)he has already logged in and tries to open /login
    *
    * @param beforeEnterEvent
    */
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Anonymous Authentication is enabled in our Spring Security conf
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            //https://vaadin.com/docs/flow/routing/tutorial-routing-lifecycle.html
            beforeEnterEvent.rerouteTo("");
        }
    }
}