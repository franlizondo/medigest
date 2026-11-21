/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

//Importante: Importar event-action listeners, necesito saber y verificar que se ingresa.
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//IMPORTAR PAQUETES DE SERVICIO
import com.mycompany.medigest.servicios.UsuarioService;
import com.mycompany.medigest.entidades.Usuario;

public class LoginFrame extends JFrame {
    
    private JTextField txtUsuario;
    private JPasswordField txtContra;
    private JButton btnLogin;

    public LoginFrame() {
        //Configuracion basica de vantana
        setTitle("MediGest - Inicio de Sesión");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en la pantalla
        setResizable(false);         // NO CAMBIEN EL SIZE
        
        //Border layout, para ver donde se ordenan los objetos.
        setLayout(new BorderLayout());

        //Construir los paneles
        add(crearPanelTitulo(), BorderLayout.NORTH);
        add(crearPanelFormulario(), BorderLayout.CENTER);

        //Logica del boton
        configurarEventos();
    }

    //Panel superior decorativo para el título
    private JPanel crearPanelTitulo() {
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(48, 78, 78)); //Otro color de images en fotos: 66, 115, 109
        panelTitulo.setPreferredSize(new Dimension(400, 60)); 
        panelTitulo.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        JLabel lblTitulo = new JLabel("BIENVENIDO AL SISTEMA");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        
        panelTitulo.add(lblTitulo);
        return panelTitulo;
    }

    //Panel central con los campos de texto
    private JPanel crearPanelFormulario() {
        JPanel panelForm = new JPanel();
        panelForm.setLayout(null); //Grid absoluto para posicionar los elementos libremente
        panelForm.setBackground(new Color(245, 247, 250));

        //Etiqueta y Campo de Usuario
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setBounds(50, 30, 80, 25);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        panelForm.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(140, 30, 200, 25);
        panelForm.add(txtUsuario);

        //Etiqueta y Campo de Contra
        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setBounds(50, 70, 80, 25);
        lblContrasena.setFont(new Font("Arial", Font.PLAIN, 14));
        panelForm.add(lblContrasena);

        txtContra = new JPasswordField();
        txtContra.setBounds(140, 70, 200, 25);
        panelForm.add(txtContra);

        //Boton de Ingreso.
        btnLogin = new JButton("Ingresar");
        btnLogin.setBounds(140, 120, 120, 35);
        btnLogin.setBackground(new Color(66, 115, 109)); // Verde secundario
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false); // Quita el borde feo al hacer clic
        panelForm.add(btnLogin);

        return panelForm;
    }

    //Logica de autenticacion, conectada a SERVICIOS DE USUARIO
    private void configurarEventos() {
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtUsuario.getText().trim();
                String contrasena = new String(txtContra.getPassword());

                //Validacion simple de campos vacios
                if (email.isEmpty() || contrasena.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "Por favor, complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    //Instanciando los servicios
                    UsuarioService usuarioService = new UsuarioService();
                    //Consulta del usuario servicio para inicio de sesion.
                    Usuario usuarioLogueado = usuarioService.iniciarSesion(email, contrasena);

                    if (usuarioLogueado != null) {
                        //Mensaje informativo con el nombre de pila / apellido real de la base de datos
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "¡Inicio de sesión exitoso!\nBienvenido/a " + usuarioLogueado.getNombreApellido(), 
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        
                        //Redireccion basada en herencia (instanceof) ; mas abajo esta el metodo.
                        abrirVentanaUsuario(usuarioLogueado);
                    } else {
                        //Credenciales incorrectas o usuario dado de baja (activo = 0)
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "El correo electrónico o la contraseña son incorrectos.", 
                            "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IllegalArgumentException exVal) {
                    //Captura validaciones de negocio lanzadas por el Service
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        exVal.getMessage(), "Datos Inválidos", JOptionPane.WARNING_MESSAGE);
                
                }   catch (Exception ex) {
                    //Si XAMPP esta apagado o la base de datos no existe/cambio de nombre
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "Error crítico al conectar con la base de datos:\n" + ex.getMessage(), 
                        "Error de Servidor", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    //Metodo clave para cerrar el Login y abrir la pantalla correcta usando la entidad de negocio
    private void abrirVentanaUsuario(Usuario usuario) {
        //Ocultamos y destruimos la ventana de Login para liberar memoria
        this.dispose(); 

        //Evaluamos el tipo de clase hija usando instanceof
        if (usuario instanceof com.mycompany.medigest.entidades.Administrador) {
            //Tu constructor pide un String (nombreUsuario), usamos getNombreApellido()
            new VentanaAdmin(usuario.getNombreApellido()).setVisible(true);
            
        } else if (usuario instanceof com.mycompany.medigest.entidades.Medico) {
            System.out.println("Abriendo interfaz del Médico para: " + usuario.getNombreApellido());
            //TODO: En el futuro descomentar e inicializar:
            new VentanaMedico(usuario.getNombreApellido()).setVisible(true);
            
        } else if (usuario instanceof com.mycompany.medigest.entidades.Recepcionista) {
            System.out.println("Abriendo interfaz de Recepción para: " + usuario.getNombreApellido());
            //TODO: En el futuro descomentar e inicializar:
            new VentanaRecepcionista(usuario.getNombreApellido()).setVisible(true);
            
        } else {
            //Ventana base generica, solo en caso de que el usuario no exista.
            new VentanaBase(usuario.getNombreApellido(), "USUARIO").setVisible(true);
        }
    }

    //Metodo main para ejecutar y probar el Login directamente
    public static void main(String[] args) {
        //Asegura que la UI se ejecute en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
    

