package fr.kybox.spring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@Route
@Push
@StyleSheet("frontend://styles/style.css")
public class MainView extends VerticalLayout {

    private final UnicastProcessor<ChatMessage> publisher;
    private final Flux<ChatMessage> messages;
    private String userName;

    public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {

        this.publisher = publisher;
        this.messages = messages;

        addClassName("main-view");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 header = new H1("KyChat");
        header.getElement().getThemeList().add("dark");

        add(header);

        askUserName();

    }

    private void askUserName() {

        HorizontalLayout layout = new HorizontalLayout();
        TextField userNameField = new TextField();
        Button startButton = new Button("Start chat");
        layout.add(userNameField, startButton);

        startButton.addClickListener(c -> {
            userName = userNameField.getValue();
            remove(layout);
            showChat();
        });

        add(layout);
    }

    private void showChat() {

        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());
        expand(messageList);

        messages.subscribe(m -> {
            getUI().ifPresent(ui -> {
                ui.access(() ->
                        messageList.add(new Paragraph(m.getFrom() + " : " + m.getMessage())));
            });
        });
    }

    private Component createInputLayout() {

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("80%");

        TextField msgField = new TextField();
        Button sendButton = new Button("Send");

        layout.add(msgField, sendButton);
        layout.expand(msgField);

        sendButton.addClickListener(c -> {
            publisher.onNext(new ChatMessage(userName, msgField.getValue()));
            msgField.clear();
            msgField.focus();
        });

        msgField.focus();

        return layout;
    }

}
