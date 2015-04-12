package tw.edu.nctu.cs.pet;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/**
 * This application that requires the following additional files:
 *   TreeDemoHelp.html
 *    arnold.html
 *    bloch.html
 *    chan.html
 *    jls.html
 *    swingtutorial.html
 *    tutorial.html
 *    tutorialcont.html
 *    vm.html
 */
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;

public class TreeDemo extends JPanel
                      implements TreeSelectionListener {
    private JEditorPane htmlPane;
    private JTree tree;
    private URL helpURL;
    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
    
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;
    
    private static ArrayList<Node> node_list = new ArrayList<Node>(); 
    private static ArrayList<Node> already_set_node_list = new ArrayList<Node>(); 
    private static ArrayList<String> already_set_list = new ArrayList<String>();
    private static ArrayList<String> have_to_set_list = new ArrayList<String>();
    
    
    //Data for input to trace tree
    private String document_name = "";
    
    public void setDocName(String s){
    	this.document_name = s;
    }
    
    public String getDocName(){
    	return this.document_name;
    }

    public TreeDemo() {
        super(new GridLayout(1,0));

        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode(this.getDocName());
        //createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
    }
    
    
    public TreeDemo(String trace_doc, ArrayList<TagInfo> tag_list) {
        super(new GridLayout(1,0));
        
        /*for(int i=0;i<tag_list.size();i++){
        	Node tmp = new Node();
        	tmp.setContent(tag_list.get(i).getTransFrom().getDeviceId());
        	
        	Node tmp2 = new Node();
        	tmp2.setContent(tag_list.get(i).getTransTo().getDeviceId());
        	
        	tmp.addChild(tmp2);
        	
        	node_list.add(tmp);
        }
        
        while(already_set_node_list.size()<node_list.size()){
        	
        	for(int i=0;i<node_list)
        	
        }*/

        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode(trace_doc);
        //addFirstLevelNodes(top, tag_list);
        //createNodes(top);
        
        
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        
        //ArrayList<String> in_tree = new ArrayList<String>();
        //ArrayList<ArrayList<Integer>> node_index = new ArrayList<ArrayList<Integer>>();
        
        for(int i=0;i<tag_list.size();i++){
        	
        	String from = tag_list.get(i).getTransFrom().getDeviceId();
        	
        	ArrayList<String> to = new ArrayList<String>();
        	
        	for(int j=0;j<tag_list.get(i).getTransTo().size();j++){
				
				to.add(tag_list.get(i).getTransTo().get(j).getDeviceId());
        		
			}
        	
        	//String to = tag_list.get(i).getTransTo().getDeviceId();
        	String time = sdFormat.format(tag_list.get(i).getTransTime());
        	
        	Enumeration en = top.depthFirstEnumeration();
        	
        	boolean in_tree = false;
        	int count = 0;
        	while (en.hasMoreElements()) {
        		count++;
        		System.out.println(count);
        	  // Unfortunately the enumeration isn't genericised so we need to downcast
        	  // when calling nextElement():
        	  DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
        	  if(node.getUserObject().toString().compareTo(from)==0){
        		  System.out.println(56);
        		  
        		  for(int j=0;j<tag_list.get(i).getTransTo().size();j++){
      				
        			  DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(tag_list.get(i).getTransTo().get(j).getDeviceId());
            		  node.add(tmp);
              		
        		  }
        		    
        		  in_tree = true;
        	  }
        	}
        	
        	if(!in_tree){
        		DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(from);
        		
        		for(int j=0;j<to.size();j++){
        			
        			DefaultMutableTreeNode tmp2 = new DefaultMutableTreeNode(to.get(j));
            		tmp.add(tmp2);
        			
        		}
        		
        		top.add(tmp);
        		
        	}
        	
        }
        
      

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        //splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
    }
    
    public TreeDemo(ArrayList<TagInfo> tag_list) {
        super(new GridLayout(1,0));
        
        
        //==========Device Information==========
        
        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("The Device receive this document");

        
        
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        
        for(int i=0;i<tag_list.size();i++){
        	
        	ArrayList<DeviceInfo> to = new ArrayList<DeviceInfo>();
        	
        	for(int j=0;j<tag_list.get(i).getTransTo().size();j++){
				
				to.add(tag_list.get(i).getTransTo().get(j));
        		
			}
        	
        	//String to = tag_list.get(i).getTransTo().getDeviceId();
        	String time = sdFormat.format(tag_list.get(i).getTransTime());
        	
        	Enumeration en = top.depthFirstEnumeration();
        	
        	for(int j=0;j<to.size();j++){
        		
        		boolean in_tree = false;
        		String device_id_type = to.get(j).getIdType();
        		String device_id = to.get(j).getDeviceId();
        		String alias = to.get(j).getName();
        		String owner = to.get(j).getOwner();
        		String domain = to.get(j).getDomain();
        		
        		while (en.hasMoreElements()) {
            	  // Unfortunately the enumeration isn't genericised so we need to downcast
            	  // when calling nextElement():
            	  DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            	  if(node.getUserObject().toString().compareTo(device_id)==0){
            		    
            		  in_tree = true;
            	  }
            	}
        		
        		if(!in_tree){
        			DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(device_id);
        			DefaultMutableTreeNode tmp_time = new DefaultMutableTreeNode("Receive time: " + time);
        			DefaultMutableTreeNode tmp_id_type = new DefaultMutableTreeNode("ID Type: " + device_id_type);
        			DefaultMutableTreeNode tmp_alias = new DefaultMutableTreeNode("Alias: " + alias);
        			DefaultMutableTreeNode tmp_owner = new DefaultMutableTreeNode("Owner: " + owner);
        			DefaultMutableTreeNode tmp_domain = new DefaultMutableTreeNode("Belong domain: " + domain);
        			tmp.add(tmp_id_type);
        			tmp.add(tmp_alias);
        			tmp.add(tmp_time);
        			tmp.add(tmp_owner);
        			tmp.add(tmp_domain);
        			top.add(tmp);
        		}
        		
        	}
        	
        }
        
        
        for(int i=0;i<tag_list.size();i++){
        	
        	DeviceInfo from = tag_list.get(i).getTransFrom();
        	
        	//String to = tag_list.get(i).getTransTo().getDeviceId();
        	
        	Enumeration en = top.depthFirstEnumeration();
        	
        		
    		boolean in_tree = false;
    		String device_id_type = from.getIdType();
    		String device_id = from.getDeviceId();
    		String alias = from.getName();
    		String owner = from.getOwner();
    		String domain = from.getDomain();
    		
    		
    		while (en.hasMoreElements()) {
        	  // Unfortunately the enumeration isn't genericised so we need to downcast
        	  // when calling nextElement():
        	  DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
        	  if(node.getUserObject().toString().compareTo(device_id)==0){
        		    
        		  in_tree = true;
        	  }
        	}
    		
    		if(!in_tree){
    			DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(device_id);
    			DefaultMutableTreeNode tmp_id_type = new DefaultMutableTreeNode("ID Type: " + device_id_type);
    			DefaultMutableTreeNode tmp_alias = new DefaultMutableTreeNode("Alias: " + alias);
    			DefaultMutableTreeNode tmp_owner = new DefaultMutableTreeNode("Owner: " + owner);
    			DefaultMutableTreeNode tmp_domain = new DefaultMutableTreeNode("Belong domain: " + domain);
    			tmp.add(tmp_id_type);
    			tmp.add(tmp_alias);
    			tmp.add(tmp_owner);
    			tmp.add(tmp_domain);
    			top.add(tmp);
    		}
        	
        }
        
      

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        //splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
        
        
        
    }
    
    
    public TreeDemo(ArrayList<DomainInfo> domain_list, DomainInfo di) {
        super(new GridLayout(1,0));
        
        
        //==========Device Information==========
        
        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("The Domain Information");

        
        for(int i=0;i<domain_list.size();i++){
        	
        	DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(domain_list.get(i).getUri());
        	DefaultMutableTreeNode tmp_name = new DefaultMutableTreeNode("Domain name: " + domain_list.get(i).getName());
			tmp.add(tmp_name);
			top.add(tmp);
			
        }
      

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        //splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
        
        
        
    }
    
    public TreeDemo(ArrayList<PersonInfo> person_list, PersonInfo pi) {
        super(new GridLayout(1,0));
        
        
        //==========Person Information==========
        
        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("The Person Information");

        
        for(int i=0;i<person_list.size();i++){
        	
        	DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(person_list.get(i).getUri());
        	DefaultMutableTreeNode tmp_idType = new DefaultMutableTreeNode("ID Type: " + person_list.get(i).getIdType());
        	DefaultMutableTreeNode tmp_id = new DefaultMutableTreeNode("ID: " + person_list.get(i).getPersonId());
        	DefaultMutableTreeNode tmp_name = new DefaultMutableTreeNode("Name: " + person_list.get(i).getName());
        	DefaultMutableTreeNode tmp_domain = new DefaultMutableTreeNode("Belong Domain: " + person_list.get(i).getDomain());
        	tmp.add(tmp_idType);
        	tmp.add(tmp_id);
			tmp.add(tmp_name);
			tmp.add(tmp_domain);
			top.add(tmp);
			
        }
      

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        //splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);
        
        
        
    }
    
    
    
    
    

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            BookInfo book = (BookInfo)nodeInfo;
            displayURL(book.bookURL);
            if (DEBUG) {
                System.out.print(book.bookURL + ":  \n    ");
            }
        } else {
            displayURL(helpURL); 
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String filename) {
            bookName = book;
            bookURL = getClass().getResource(filename);
            if (bookURL == null) {
                System.err.println("Couldn't find file: "
                                   + filename);
            }
        }

        public String toString() {
            return bookName;
        }
    }

    private void initHelp() {
        String s = "TreeDemoHelp.html";
        helpURL = getClass().getResource(s);
        if (helpURL == null) {
            System.err.println("Couldn't open help file: " + s);
        } else if (DEBUG) {
            System.out.println("Help URL is " + helpURL);
        }

        displayURL(helpURL);
    }

    private void displayURL(URL url) {
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else { //null url
            	htmlPane.setText("File Not Found");
                if (DEBUG) {
                    System.out.println("Attempted to display a null URL.");
                }
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("Books for Java Programmers");
        top.add(category);

        //original Tutorial
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Tutorial: A Short Course on the Basics",
            "tutorial.html"));
        category.add(book);

        //Tutorial Continued
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Tutorial Continued: The Rest of the JDK",
            "tutorialcont.html"));
        category.add(book);

        //JFC Swing Tutorial
        book = new DefaultMutableTreeNode(new BookInfo
            ("The JFC Swing Tutorial: A Guide to Constructing GUIs",
            "swingtutorial.html"));
        category.add(book);

        //Bloch
        book = new DefaultMutableTreeNode(new BookInfo
            ("Effective Java Programming Language Guide",
	     "bloch.html"));
        category.add(book);

        //Arnold/Gosling
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Programming Language", "arnold.html"));
        category.add(book);

        //Chan
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Developers Almanac",
             "chan.html"));
        category.add(book);

        category = new DefaultMutableTreeNode("Books for Java Implementers");
        top.add(category);

        //VM
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Virtual Machine Specification",
             "vm.html"));
        category.add(book);

        //Language Spec
        book = new DefaultMutableTreeNode(new BookInfo
            ("The Java Language Specification",
             "jls.html"));
        category.add(book);
    }
        
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame("Document Flaw");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new TreeDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
}
