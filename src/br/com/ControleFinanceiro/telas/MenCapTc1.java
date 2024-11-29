package br.com.ControleFinanceiro.telas;

import java.sql.*;
import br.com.ControleFinanceiro.dal.ModuloConexao;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anderson L
 */

public class MenCapTc1 extends javax.swing.JInternalFrame {

    // Declaração das variáveis de conexão com o banco de dados e manipulação de consultas
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    // Construtor da classe, onde é feita a inicialização dos componentes e da conexão com o banco de dados
    public MenCapTc1() {
        initComponents();  // Inicializa os componentes da interface gráfica
        conexao = ModuloConexao.conector();  // Estabelece a conexão com o banco de dados
        carregarPerfis();  // Carrega os perfis de usuários do banco de dados
        carregarTabelaUsuarios();  // Carrega os dados da tabela de usuários
    }
    
    // Método para carregar os perfis de usuários no combo box cboUsuPerfil
    private void carregarPerfis() {
        String sql = "SELECT perfil FROM tbusuarios GROUP BY perfil";  // Consulta SQL para pegar os perfis distintos
        try (PreparedStatement pst = conexao.prepareStatement(sql);  // Prepara a consulta
             ResultSet rs = pst.executeQuery()) {  // Executa a consulta e armazena o resultado

            while (rs.next()) {
                cboUsuPerfil.addItem(rs.getString("perfil"));  // Adiciona os perfis ao combo box
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção na consulta ao banco
            JOptionPane.showMessageDialog(null, "Erro ao carregar perfis: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para consultar um usuário no banco de dados usando o ID
    private void consultar() {
        String sql = "SELECT * FROM tbusuarios WHERE iduser=?";  // Consulta SQL para buscar um usuário específico
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, txtUsuId.getText());  // Define o parâmetro ID do usuário
            try (ResultSet rs = pst.executeQuery()) {  // Executa a consulta
                if (rs.next()) {  // Se um usuário for encontrado
                    // Preenche os campos com as informações do usuário retornadas do banco
                    txtUsuNome.setText(rs.getString("usuario"));
                    txtUsuFone.setText(rs.getString("fone"));
                    txtUsuLogin.setText(rs.getString("login"));
                    txtUsuSenha.setText(rs.getString("senha"));
                    cboUsuPerfil.setSelectedItem(rs.getString("perfil"));
                } else {
                    // Se não encontrar o usuário, exibe mensagem de alerta e limpa os campos
                    JOptionPane.showMessageDialog(null, "Usuário não cadastrado.", "Atenção", JOptionPane.WARNING_MESSAGE);
                    limparCampos();
                }
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção na consulta ao banco
            JOptionPane.showMessageDialog(null, "Erro ao consultar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para adicionar um novo usuário ao banco de dados
    private void adicionarUsuario() {
        // Valida se todos os campos obrigatórios estão preenchidos
        if (!validarCamposObrigatorios()) {
            JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO tbusuarios(usuario, fone, login, senha, perfil) VALUES (?, ?, ?, ?, ?)";  // SQL de inserção
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            // Define os parâmetros da consulta com os valores dos campos preenchidos
            pst.setString(1, txtUsuNome.getText());
            pst.setString(2, txtUsuFone.getText());
            pst.setString(3, txtUsuLogin.getText());
            pst.setString(4, txtUsuSenha.getText());
            pst.setString(5, cboUsuPerfil.getSelectedItem().toString());

            // Executa a inserção e verifica se o usuário foi adicionado com sucesso
            int adicionado = pst.executeUpdate();
            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "Usuário adicionado com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();  // Limpa os campos após a adição
                carregarTabelaUsuarios();  // Recarrega a tabela de usuários
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção na inserção
            JOptionPane.showMessageDialog(null, "Erro ao adicionar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para atualizar os dados de um usuário
    private void atualizarUsuario() {
        // Valida se todos os campos obrigatórios estão preenchidos e se o ID do usuário não está vazio
        if (!validarCamposObrigatorios() || txtUsuId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, insira o ID do usuário e preencha todos os campos obrigatórios.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE tbusuarios SET usuario=?, fone=?, login=?, senha=?, perfil=? WHERE iduser=?";  // SQL de atualização
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            // Define os parâmetros da consulta com os valores dos campos preenchidos
            pst.setString(1, txtUsuNome.getText());
            pst.setString(2, txtUsuFone.getText());
            pst.setString(3, txtUsuLogin.getText());
            pst.setString(4, txtUsuSenha.getText());
            pst.setString(5, cboUsuPerfil.getSelectedItem().toString());
            pst.setString(6, txtUsuId.getText());

            // Executa a atualização e verifica se os dados foram atualizados com sucesso
            int atualizado = pst.executeUpdate();
            if (atualizado > 0) {
                JOptionPane.showMessageDialog(null, "Dados do usuário atualizados com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();  // Limpa os campos após a atualização
                carregarTabelaUsuarios();  // Recarrega a tabela de usuários
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção na atualização
            JOptionPane.showMessageDialog(null, "Erro ao atualizar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para excluir um usuário
    private void excluirUsuario() {
        String sql = "DELETE FROM tbusuarios WHERE iduser=?";  // SQL de exclusão
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, txtUsuId.getText());  // Define o ID do usuário a ser excluído

            // Executa a exclusão e verifica se o usuário foi excluído com sucesso
            int apagado = pst.executeUpdate();
            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "Usuário excluído com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();  // Limpa os campos após a exclusão
                carregarTabelaUsuarios();  // Recarrega a tabela de usuários
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção na exclusão
            JOptionPane.showMessageDialog(null, "Erro ao excluir usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para limpar os campos de entrada de dados
    private void limparCampos() {
        txtUsuId.setText(null);  // Limpa o campo ID
        txtUsuNome.setText(null);  // Limpa o campo Nome
        txtUsuFone.setText(null);  // Limpa o campo Fone
        txtUsuLogin.setText(null);  // Limpa o campo Login
        txtUsuSenha.setText(null);  // Limpa o campo Senha
        cboUsuPerfil.setSelectedIndex(-1);  // Limpa a seleção do combo box de perfil
    }
    
    // Método para carregar os usuários cadastrados na tabela
    private void carregarTabelaUsuarios() {
        String sql = "SELECT iduser, usuario FROM tbusuarios";  // SQL para buscar os usuários cadastrados
        try (PreparedStatement pst = conexao.prepareStatement(sql); 
             ResultSet rs = pst.executeQuery()) {  // Executa a consulta

            // Obtém o modelo da tabela e limpa as linhas anteriores
            DefaultTableModel model = (DefaultTableModel) tbliduser.getModel();
            model.setNumRows(0);

            // Preenche a tabela com os dados dos usuários
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("iduser"),  // Adiciona o ID do usuário
                    rs.getString("usuario")  // Adiciona o nome do usuário
                });
            }
        } catch (SQLException e) {
            // Exibe mensagem de erro caso ocorra uma exceção ao carregar a tabela
            JOptionPane.showMessageDialog(null, "Erro ao carregar dados da tabela: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para validar se os campos obrigatórios estão preenchidos
    private boolean validarCamposObrigatorios() {
        return !txtUsuNome.getText().isEmpty() &&  // Verifica se o nome não está vazio
               !txtUsuLogin.getText().isEmpty() &&  // Verifica se o login não está vazio
               !txtUsuSenha.getText().isEmpty();  // Verifica se a senha não está vazia
    }




    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cboUsuPerfil = new javax.swing.JComboBox<>();
        txtUsuNome = new javax.swing.JTextField();
        txtUsuLogin = new javax.swing.JTextField();
        txtUsuSenha = new javax.swing.JTextField();
        txtUsuId = new javax.swing.JTextField();
        btnUsuCreate = new javax.swing.JButton();
        btnUsuRead = new javax.swing.JButton();
        btnUsuUpdate = new javax.swing.JButton();
        btnUsuDelete = new javax.swing.JButton();
        txtUsuFone = new javax.swing.JTextField();
        btnUsuLimpar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbliduser = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Tela de Usuários");
        setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setText("*Iduser");

        jLabel2.setText("*Usuário");

        jLabel3.setText("Fone");

        jLabel4.setText("*Login");

        jLabel5.setText("*Senha");

        jLabel6.setText("*Perfil");

        btnUsuCreate.setText("Adicionar");
        btnUsuCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuCreateActionPerformed(evt);
            }
        });

        btnUsuRead.setText("Consultar");
        btnUsuRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuReadActionPerformed(evt);
            }
        });

        btnUsuUpdate.setText("Alterar");
        btnUsuUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuUpdateActionPerformed(evt);
            }
        });

        btnUsuDelete.setText("Excluir");
        btnUsuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuDeleteActionPerformed(evt);
            }
        });

        btnUsuLimpar.setText("Limpar");
        btnUsuLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnUsuCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUsuRead)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUsuUpdate)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboUsuPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(26, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnUsuDelete)
                                .addGap(18, 18, 18)
                                .addComponent(btnUsuLimpar)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtUsuId, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(24, 24, 24)
                                .addComponent(txtUsuSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(31, 31, 31)
                                .addComponent(txtUsuLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtUsuNome, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                                    .addComponent(txtUsuFone))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6)
                    .addComponent(cboUsuPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsuId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUsuNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtUsuFone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtUsuLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtUsuSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUsuCreate)
                    .addComponent(btnUsuRead)
                    .addComponent(btnUsuUpdate)
                    .addComponent(btnUsuDelete)
                    .addComponent(btnUsuLimpar))
                .addGap(66, 66, 66))
        );

        tbliduser.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Usuários"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbliduser);

        jLabel7.setText("* Campos Obrigatórios");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(118, Short.MAX_VALUE))
        );

        setBounds(0, 0, 883, 563);
    }// </editor-fold>//GEN-END:initComponents

    private void btnUsuCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuCreateActionPerformed
        // Botão para adicionar usuário
            try {
        adicionarUsuario(); // Chama o método de adicionar usuário
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao adicionar usuário: " + e.getMessage());
    }
    }//GEN-LAST:event_btnUsuCreateActionPerformed

    private void btnUsuReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuReadActionPerformed
        // Botão para consultar usuário
            try {
        if (txtUsuId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, insira o ID do usuário para a consulta.");
        } else {
            consultar(); // Chama o método de consultar usuário
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao consultar usuário: " + e.getMessage());
    }
        
    }//GEN-LAST:event_btnUsuReadActionPerformed

    private void btnUsuUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuUpdateActionPerformed
        // Botão para atualizar usuário
            try {
        if (txtUsuId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, consulte o usuário antes de atualizar.");
        } else {
            atualizarUsuario(); // Chama o método de atualizar usuário
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao atualizar usuário: " + e.getMessage());
    }
    }//GEN-LAST:event_btnUsuUpdateActionPerformed

    private void btnUsuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuDeleteActionPerformed
        // Botão para excluir usuário
            try {
        if (txtUsuId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, consulte o usuário antes de excluir.");
        } else {
            int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja excluir este usuário?", "Atenção", JOptionPane.YES_NO_OPTION);
            if (confirma == JOptionPane.YES_OPTION) {
                excluirUsuario(); // Chama o método de excluir usuário
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao excluir usuário: " + e.getMessage());
    }
    }//GEN-LAST:event_btnUsuDeleteActionPerformed

    private void btnUsuLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuLimparActionPerformed
            // Botão para limpar campos
    limparCampos(); // Chama o método de limpar campos
    }//GEN-LAST:event_btnUsuLimparActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnUsuCreate;
    private javax.swing.JButton btnUsuDelete;
    private javax.swing.JButton btnUsuLimpar;
    private javax.swing.JButton btnUsuRead;
    private javax.swing.JButton btnUsuUpdate;
    private javax.swing.JComboBox<String> cboUsuPerfil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbliduser;
    private javax.swing.JTextField txtUsuFone;
    private javax.swing.JTextField txtUsuId;
    private javax.swing.JTextField txtUsuLogin;
    private javax.swing.JTextField txtUsuNome;
    private javax.swing.JTextField txtUsuSenha;
    // End of variables declaration//GEN-END:variables
}
