package org.pat.causeconnect.ui.task;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.MainLayout;
import org.pat.causeconnect.ui.component.CardComponent;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "tasks", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Mes Tâches")
public class TasksView extends VerticalLayout {
    public TasksView(TaskService taskService) {

        setSizeFull();
//        ArrayList<Task> tasks = taskService.getMyTasks();

        add(new H2("Mes tâches"));

//        FlexLayout taskList = new FlexLayout();
//        taskList.setWidthFull();
//        taskList.setFlexWrap(FlexLayout.FlexWrap.WRAP);
//
//        List<Task> sortedTasks = tasks.stream()
//                .sorted(Comparator.comparing(Task::getDeadline))
//                .toList();
//        sortedTasks.forEach(task -> {
//            Date endTime = task.getDeadline();
//            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM", Locale.FRENCH);
//            CardComponent taskDiv = new CardComponent(task.getProject().getName() + " : " +task.getTitle(), "Échéance : " + formatter.format(endTime));
//            ComponentEventListener<AttachEvent> listener = event -> {
//                taskDiv.addClickListener(e -> {
//                    getUI().ifPresent(ui -> ui.navigate(TaskView.class, task.getId()));
//                });
//            };
//            taskDiv.addAttachListener(listener);
//
//            taskList.add(taskDiv);
//        });
//
//        add(taskList);
        Map<String, List<Task>> taskByProject = taskService.getMyTasks().stream()
                .sorted(Comparator.comparing(Task::getDeadline))
                .collect(Collectors.groupingBy(task -> task.getProject().getName()));

        taskByProject.forEach((projectName, tasks) -> {
            Details projectSection = new Details();
            projectSection.setSummaryText(projectName);
            projectSection.setOpened(true);

            Button createTaskButton = new Button("Créer une tâche");
            createTaskButton.setIcon(VaadinIcon.PLUS.create());
            createTaskButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            projectSection.add(createTaskButton);

            FlexLayout taskList = new FlexLayout();
            taskList.setWidthFull();
            taskList.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            taskList.setFlexDirection(FlexLayout.FlexDirection.ROW);

            tasks.forEach(task -> {
                Date endTime = task.getDeadline();
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM", Locale.FRENCH);
                CardComponent taskDiv = new CardComponent(task.getTitle(), "Échéance : " + formatter.format(endTime));
                ComponentEventListener<AttachEvent> listener = event -> taskDiv.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(TaskView.class, task.getId())));
                taskDiv.addAttachListener(listener);

                taskList.add(taskDiv);
            });

            projectSection.add(taskList);

            add(projectSection);
        });
    }
}