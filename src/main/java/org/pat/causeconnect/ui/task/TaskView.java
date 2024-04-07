package org.pat.causeconnect.ui.task;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.MainLayout;

@Route(value = "tasks", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Tâche")
public class TaskView extends VerticalLayout implements HasUrlParameter<String> {

    private final TaskService taskService;

    public TaskView(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        loadTask(parameter);
    }

    private void loadTask(String taskId) {
        Task task = taskService.getTaskById(taskId);
        add(task.getTitle());
    }
}
