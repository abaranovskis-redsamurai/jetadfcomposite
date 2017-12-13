package com.redsamurai.view.beans;

import com.redsamurai.view.utils.ADFUtils;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.render.ClientEvent;

import oracle.jbo.Key;

import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

public class JetController {
    public JetController() {
        super();
    }

    public void employeeReviewOpenListener(DisclosureEvent disclosureEvent) {
        this.reDrawJet();
    }

    public void employeeSearchListener(QueryEvent queryEvent) {
        invokeMethodExpression("#{bindings.EmployeesViewCriteriaQuery.processQuery}", Object.class, QueryEvent.class, queryEvent);

        this.reDrawJet();
    }
    
    public void loadMoreListener(ActionEvent actionEvent) {
        Integer count = (Integer) ADFContext.getCurrent()
                                            .getPageFlowScope()
                                            .get("searchRowsCount");
        
        if (count != null) {
            ADFContext.getCurrent().getPageFlowScope().put("searchRowsCount", new Integer(count) + 5);
        } else {
            ADFContext.getCurrent().getPageFlowScope().put("searchRowsCount", 10);
        }
        
        this.reDrawJet();
    }
    
    public void jsServerCallListener(ClientEvent clientEvent) {
        Integer emplKey = ((Double)clientEvent.getParameters().get("jetParam")).intValue();
        
        Integer[] keys = new Integer[1];
        keys[0] = emplKey;
        
        Key key = new Key(keys);
        ADFUtils.findIterator("EmployeesView1Iterator").setCurrentRowWithKey(key.toStringFormat(true));
    }
    
    private void reDrawJet() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
        erks.addScript(fctx, "pprEnforceJet()");
    }

    private Object invokeMethodExpression(String expr, Class returnType, Class argType, Object argument) {
        return invokeMethodExpression(expr, returnType, new Class[] { argType }, new Object[] { argument });
    }

    private Object invokeMethodExpression(String expr, Class returnType, Class[] argTypes, Object[] args) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory();
        MethodExpression methodExpr = elFactory.createMethodExpression(elctx, expr, returnType, argTypes);
        return methodExpr.invoke(elctx, args);
    }
}
