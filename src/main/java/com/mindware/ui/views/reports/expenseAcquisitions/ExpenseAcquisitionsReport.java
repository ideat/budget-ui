package com.mindware.ui.views.reports.expenseAcquisitions;


import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Route(value = "expense-acquisition-report", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Reportes")

public class ExpenseAcquisitionsReport extends ViewFrame implements RouterLayout {

    private static final String EXPECTED_ACQUISITION_RESUME ="RESUMEN EJECUTIVO";

    @Autowired
    UtilValues utilValues;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setViewContent(createContent());
    }

    private Accordion createOptionsReport(){
        Accordion optionDetails = new Accordion();

        AccordionPanel resumeExpenseAcquisitionPanel = optionDetails.add(EXPECTED_ACQUISITION_RESUME,layoutResumeExpenseAcquisition());


        return optionDetails;
    }

    private HorizontalLayout layoutResumeExpenseAcquisition(){

        ComboBox<String> periods = new ComboBox<>();
        periods.setWidth("200px");
        periods.setItems(utilValues.getAllPeriods());
        periods.setAllowCustomValue(false);
        periods.setClearButtonVisible(true);
        periods.setPlaceholder("Seleccione Periodo");

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST);
        print.setIcon(VaadinIcon.PRINT.create());
        print.addClickListener(event ->{

            Map<String, List<String>> paramExpenseAcquisition = new HashMap<>();
            List<String> origin = new ArrayList<>();
            origin.add("expense-acquisition-report");
            List<String> path = new ArrayList<>();
            path.add("expense-acquisition-report");
            List<String> period = new ArrayList<>();
            period.add(periods.getValue());

            paramExpenseAcquisition.put("origin",origin);
            paramExpenseAcquisition.put("path",path);
            paramExpenseAcquisition.put("period",period);
            QueryParameters qp = new QueryParameters(paramExpenseAcquisition);
            UI.getCurrent().navigate("report-preview", qp);

        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.add(periods,print);

        return layout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createOptionsReport());
//        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }
}
