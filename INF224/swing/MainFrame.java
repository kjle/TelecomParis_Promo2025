import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private JPanel mainPanel;
    private Client client = null; 
    boolean connected = false;

    public MainFrame() {
        // initialize the frame
        setTitle("MultiMedia Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create the main panel
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu connectionMenu = new JMenu("Connection");
        JMenuItem connectMenuItem = new JMenuItem("Settings");

        JMenu objectMenu = new JMenu("Object");
        JMenuItem photoObjectMenu = new JMenuItem("Photo");
        JMenuItem videoObjectMenu = new JMenuItem("Video");
        JMenuItem filmObjectMenu = new JMenuItem("Film");

        JMenu groupMenu = new JMenu("Group");
        JMenuItem groupMenuItem = new JMenuItem("Group");

        JMenu systemMenu = new JMenu("System");
        JMenuItem quitMenuItem = new JMenuItem("Quit");

        connectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToConnectionLayout();
            }
        });

        photoObjectMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToObjectLayout("Photo");
            }
        });

        videoObjectMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToObjectLayout("Video");
            }
        });

        filmObjectMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToObjectLayout("Film");
            }
        });

        groupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeToGroupLayout();
            }
        });

        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        connectionMenu.add(connectMenuItem);
        objectMenu.add(photoObjectMenu);
        objectMenu.add(videoObjectMenu);
        objectMenu.add(filmObjectMenu);
        groupMenu.add(groupMenuItem);
        systemMenu.add(quitMenuItem);

        menuBar.add(connectionMenu);
        menuBar.add(objectMenu);
        menuBar.add(groupMenu);
        menuBar.add(systemMenu);

        setJMenuBar(menuBar);

        setVisible(true);
    }

    private void changeToConnectionLayout() {
        mainPanel.removeAll();

        JPanel connectionPanel = new JPanel(new GridLayout(8,1));
        JLabel ipLabel = new JLabel("IP Address:");
        JTextField ipTextField = new JTextField("localhost");
        JLabel portLabel = new JLabel("Port:");
        JTextField portTextField = new JTextField("3331");
        JButton connectButton = new JButton();
        if (connected) {
            connectButton.setText("Disconnect");
        } else {
            connectButton.setText("Connect");
        }

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(client == null || !connected) {
                    try {
                        client = new Client(ipTextField.getText(), Integer.parseInt(portTextField.getText()));
                        connected = true;
                        connectButton.setText("Disconnect");
                    } catch (Exception ex) {
                        System.out.println("Connecting error");
                    }
                } else {
                    client.send("stop");
                    connected = false;
                    client = null;
                    connectButton.setText("Connect");
                }
            }
        });

        connectionPanel.add(ipLabel);
        connectionPanel.add(ipTextField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portTextField);
        
        mainPanel.add(connectionPanel, BorderLayout.CENTER);
        mainPanel.add(connectButton, BorderLayout.SOUTH);

        mainPanel.revalidate();
        mainPanel.repaint();
    
    }

    private void changeToObjectLayout(String o) {
        
        mainPanel.removeAll(); 

        if (connected) {

            JPanel objectPanel = new JPanel(new BorderLayout());
            JPanel commandPanel = new JPanel(new GridLayout(3, 1));
            JPanel textJPanel = new JPanel(new BorderLayout());
            JTextField searchTextField = new JTextField(20);

            JTextArea textArea = new JTextArea(10, 30);
            if (o.equals("Photo")) {
                textArea.setText( "Available " + o + " : \ntest.png\n");
                searchTextField.setText("test.png");
            } else if (o.equals("Video")) {
                textArea.setText( "Available " + o + " : \ntest.mp4\n");
                searchTextField.setText("test.mp4");
            } else if (o.equals("Film")) {
                textArea.setText( "Available " + o + " : \ntest.mkv\n");
                searchTextField.setText("test.mkv");
            }
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            textJPanel.add(scrollPane, BorderLayout.CENTER);

            JButton playButton = new JButton("Play");
            JButton searchButton = new JButton("Search");
            commandPanel.add(searchTextField);
            commandPanel.add(playButton);
            commandPanel.add(searchButton);

            playButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = "play " + searchTextField.getText();
                    client.send(cmd);
                }
            });

            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = "search " + searchTextField.getText();
                    String response = client.send(cmd);
                    textArea.append(response.replace("-","\n"));
                }
            });

            objectPanel.add(commandPanel, BorderLayout.EAST);
            objectPanel.add(textJPanel, BorderLayout.CENTER);

            mainPanel.add(objectPanel, BorderLayout.CENTER);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void changeToGroupLayout() {
        mainPanel.removeAll(); 

        if (connected) {
            JPanel groupPanel = new JPanel(new BorderLayout());
            JPanel commandPanel = new JPanel(new GridLayout(4, 1));
            JPanel textJPanel = new JPanel(new BorderLayout());

            JTextField searchTextField = new JTextField(20);
            searchTextField.setText("testgroup");
        
            JTextArea textArea = new JTextArea(10, 30);
            textArea.setText( "Available Group : \ntestgroup\n");
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            textJPanel.add(scrollPane, BorderLayout.CENTER);

            // JButton createButton = new JButton("Create");
            JButton searchButton = new JButton("Search");

            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = "search group " + searchTextField.getText();
                    String response = client.send(cmd);
                    textArea.append(response.replace("-","\n"));
                }
        
            });

            commandPanel.add(searchTextField);
            // commandPanel.add(createButton);
            commandPanel.add(searchButton);

            groupPanel.add(commandPanel, BorderLayout.EAST);
            groupPanel.add(textJPanel, BorderLayout.CENTER);

            mainPanel.add(groupPanel, BorderLayout.CENTER);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}
