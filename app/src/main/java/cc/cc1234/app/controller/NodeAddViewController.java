package cc.cc1234.app.controller;

import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.PathConcat;
import cc.cc1234.app.view.NodeDataArea;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.specification.node.NodeMode;
import cc.cc1234.specification.node.ZkNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

public class NodeAddViewController {

    @FXML
    private TextField nodeNameTextField;

    @FXML
    private CheckBox isNodeSeq;

    @FXML
    private CheckBox isNodeEph;

    @FXML
    private TextField currentPathField;

    @FXML
    private AnchorPane nodeAddPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private NodeDataArea dataCodeArea = new NodeDataArea();

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        cancelButton.setOnMouseClicked(e -> hide());
        confirmButton.setOnMouseClicked(e -> onSave());

        var pane = new VirtualizedScrollPane<>(dataCodeArea);
        AnchorPane.setTopAnchor(pane, 155d);
        AnchorPane.setLeftAnchor(pane, 70d);
        AnchorPane.setRightAnchor(pane, 70d);
        AnchorPane.setBottomAnchor(pane, 55d);
        nodeAddPane.getChildren().add(pane);
    }

    public void show(StackPane parent) {
        show(parent, null);
    }

    public void show(StackPane parent, ZkNode zkNode) {
        if (!parent.getChildren().contains(nodeAddPane)) {
            parent.getChildren().add(nodeAddPane);
            Transitions.zoomInY(nodeAddPane).playFromStart();
        }
        String parentPath = zkNode == null ? "/" : zkNode.getPath();
        currentPathField.setText(parentPath);
    }

    public void hide() {
        Transitions.zoomOutY(nodeAddPane, event -> {
            final StackPane parent = (StackPane) nodeAddPane.getParent();
            if (parent != null && parent.getChildren().contains(nodeAddPane)) {
                parent.getChildren().remove(nodeAddPane);
            }
        }).playFromStart();
    }

    public void onSave() {
        String server = ActiveServerContext.get();
        final NodeMode mode = createMode();
        String path = PathConcat.concat(currentPathField.getText(), nodeNameTextField.getText());
        String data = dataCodeArea.getText();
        Try.of(() -> prettyZooFacade.createNode(server, path, data, mode))
                .onSuccess(r -> {
                    hide();
                    VToast.info("success");
                })
                .onFailure(e -> VToast.error(e.getMessage()));
    }

    private NodeMode createMode() {
        if (isNodeEph.isSelected() && isNodeSeq.isSelected()) {
            return NodeMode.EPHEMERAL_SEQUENTIAL;
        }

        if (isNodeSeq.isSelected()) {
            return NodeMode.PERSISTENT_SEQUENTIAL;
        }

        if (isNodeEph.isSelected()) {
            return NodeMode.EPHEMERAL;
        }

        // TODO  how to support CreateMode.CONTAINER ?
        return NodeMode.PERSISTENT;
    }
}
