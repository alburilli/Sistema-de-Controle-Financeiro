package br.com.ControleFinanceiro.telas;

import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anderson L
 */

public class MenCapTc2 extends javax.swing.JInternalFrame {
    private TelaPrincipal telaPrincipal;  // Campo para armazenar a instância de TelaPrincipal

    // Variáveis de conexão com o banco
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public MenCapTc2(TelaPrincipal telaPrincipal) {
        initComponents();
        this.telaPrincipal = telaPrincipal;  // Atribui a instância de TelaPrincipal ao campo
        conexao = ModuloConexao.conector(); // Conecta com o banco de dados
        popularTabela(); // Popula a tabela de descrições
        popularComboBox(); // Popula o combo box com os tipos
    }


    // Método para inserir nova descrição
private void inserirDescricao(String descricao, int tipoId, Date dataVencimento) {
    String sql = "INSERT INTO descricoes (descricao, tipo_id, data_vencimento) VALUES (?, ?, ?)";
    
    try {
        pst = conexao.prepareStatement(sql);
        pst.setString(1, descricao);
        pst.setInt(2, tipoId);

        if (dataVencimento != null) {
            pst.setDate(3, new java.sql.Date(dataVencimento.getTime()));
        } else {
            pst.setNull(3, java.sql.Types.DATE);
        }

        pst.executeUpdate();
        JOptionPane.showMessageDialog(null, "Descrição adicionada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        // Atualizar a tabela de vencimentos
        telaPrincipal.atualizarTabelaVencimentos();

    } catch (SQLIntegrityConstraintViolationException e) {
        JOptionPane.showMessageDialog(this, "Erro: já existe uma descrição com este nome. Verifique e tente novamente.", "Erro de Conflito", JOptionPane.WARNING_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao adicionar descrição: problema de conexão ou de dados incorretos.\nDetalhes técnicos: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro inesperado ao adicionar descrição. Por favor, contate o suporte.\nDetalhes técnicos: " + e.getMessage(), "Erro Desconhecido", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        fecharRecursos();
    }
}

// Método para excluir descrição
private void excluirDescricao(int descricaoId) {
    String sql = "DELETE FROM descricoes WHERE id = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setInt(1, descricaoId);
        int linhasAfetadas = pst.executeUpdate();

        if (linhasAfetadas > 0) {
            JOptionPane.showMessageDialog(null, "Descrição excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            telaPrincipal.atualizarTabelaVencimentos();
        } else {
            JOptionPane.showMessageDialog(this, "Descrição não encontrada. Por favor, verifique o ID.", "Erro de Exclusão", JOptionPane.WARNING_MESSAGE);
        }
    } catch (SQLIntegrityConstraintViolationException e) {
        JOptionPane.showMessageDialog(this, "Erro: não é possível excluir uma descrição associada a transações existentes.", "Erro de Restrição", JOptionPane.WARNING_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao excluir descrição. Por favor, verifique os dados e tente novamente.\nDetalhes técnicos: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro inesperado ao excluir descrição. Contate o suporte.\nDetalhes técnicos: " + e.getMessage(), "Erro Desconhecido", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        fecharRecursos();
    }
}

// Método para atualizar descrição
private void atualizarDescricao(int descricaoId, String novaDescricao, int novoTipoId, Date novaDataVencimento) {
    String sql = "UPDATE descricoes SET descricao = ?, tipo_id = ?, data_vencimento = ? WHERE id = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setString(1, novaDescricao);
        pst.setInt(2, novoTipoId);

        if (novaDataVencimento != null) {
            pst.setDate(3, new java.sql.Date(novaDataVencimento.getTime()));
        } else {
            pst.setNull(3, java.sql.Types.DATE);
        }
        pst.setInt(4, descricaoId);

        int linhasAfetadas = pst.executeUpdate();
        if (linhasAfetadas > 0) {
            JOptionPane.showMessageDialog(null, "Descrição atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            telaPrincipal.atualizarTabelaVencimentos();
        } else {
            JOptionPane.showMessageDialog(this, "Descrição não encontrada para atualização. Verifique o ID.", "Erro de Atualização", JOptionPane.WARNING_MESSAGE);
        }
    } catch (SQLIntegrityConstraintViolationException e) {
        JOptionPane.showMessageDialog(this, "Erro: já existe uma descrição com esse nome. Verifique os dados e tente novamente.", "Erro de Conflito", JOptionPane.WARNING_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao atualizar descrição. Dados inválidos ou problema de conexão.\nDetalhes técnicos: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro inesperado ao atualizar descrição. Contate o suporte.\nDetalhes técnicos: " + e.getMessage(), "Erro Desconhecido", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        fecharRecursos();
    }
}


private void popularTabela() {
    DefaultTableModel model = (DefaultTableModel) tblDescricoes.getModel();
    model.setRowCount(0); // Limpa a tabela antes de adicionar novas linhas

    String sql = "SELECT d.id, d.descricao, d.tipo_id, t.nome AS tipo, d.data_vencimento FROM descricoes d " +
                 "INNER JOIN tipos t ON d.tipo_id = t.id ORDER BY d.descricao ASC";
    try {
        pst = conexao.prepareStatement(sql);
        rs = pst.executeQuery();

        while (rs.next()) {
            // Captura os valores das colunas, com tratamento para valores nulos
            Object[] rowData = new Object[5];
            rowData[0] = rs.getInt("id");
            rowData[1] = rs.getString("descricao") != null ? rs.getString("descricao") : "N/A";
            rowData[2] = rs.getInt("tipo_id");
            rowData[3] = rs.getString("tipo") != null ? rs.getString("tipo") : "N/A";
            rowData[4] = rs.getDate("data_vencimento") != null ? rs.getDate("data_vencimento") : null;

            // Adiciona a linha ao modelo de tabela
            model.addRow(rowData);
        }
        
        // Oculta a coluna de ID (primeira coluna)
        tblDescricoes.getColumnModel().getColumn(0).setMinWidth(0);
        tblDescricoes.getColumnModel().getColumn(0).setMaxWidth(0);
        tblDescricoes.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        // Oculta a coluna tipo_id (segunda coluna)
        tblDescricoes.getColumnModel().getColumn(2).setMinWidth(0);
        tblDescricoes.getColumnModel().getColumn(2).setMaxWidth(0);
        tblDescricoes.getColumnModel().getColumn(2).setPreferredWidth(0);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao popular tabela de descrições: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
        }
    }
}

    // Método para popular o combo box com os tipos
    private void popularComboBox() {
        String sql = "SELECT * FROM tipos ORDER BY nome ASC";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();

            cmbTipos.removeAllItems(); // Limpa o combo box
            while (rs.next()) {
                cmbTipos.addItem(rs.getString("nome")); // Adiciona tipos ao combo box
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao popular combo box: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }


    // Método para obter o ID do tipo selecionado
    private int getTipoId(String tipo) {
        int tipo_id = 0;
        String sql = "SELECT id FROM tipos WHERE nome = ?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, tipo);
            rs = pst.executeQuery();

            if (rs.next()) {
                tipo_id = rs.getInt("id");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao obter ID do tipo: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
            }
        }
        return tipo_id;
    }


    // Método para limpar os campos após a adição
private void limparCampos() {
    txtDescricao.setText(null);
    cmbTipos.setSelectedIndex(0); // Reseta o combo box
    jDateChooser1.setDate(null); // Limpa a data
}


    // Método para obter o ID da descrição selecionada
private int obterIdDescricao(String descricao) {
    int id = 0;
    String sql = "SELECT id FROM descricoes WHERE descricao = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setString(1, descricao);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            id = rs.getInt("id");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao obter ID da descrição: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
        }
    }
    return id;
}

// Método para obter o nome do tipo com base no ID
private String getTipoNome(int tipoId) {
    String tipoNome = "";
    String sql = "SELECT nome FROM tipos WHERE id = ?";
    try {
        pst = conexao.prepareStatement(sql);
        pst.setInt(1, tipoId);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            tipoNome = rs.getString("nome");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Erro ao obter nome do tipo: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
        }
    }
    return tipoNome;
}

private void fecharRecursos() {
    try {
        if (pst != null) pst.close();
        if (rs != null) rs.close();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao liberar recursos de banco de dados.\nDetalhes técnicos: " + e.getMessage(), "Erro de Fechamento", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}


 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtDescricao = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDescricoes = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbTipos = new javax.swing.JComboBox<>();
        btnAdicionar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        btnAtualizar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Tela descrições");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Relacionar um Tipo nas Descrições");

        jLabel2.setText("Descrições");

        tblDescricoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID da descrição", "Tipo da descrições", "Tipo ID", "Nome do tipo", "Data de vencimento"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDescricoes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDescricoesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDescricoes);

        jLabel3.setText("Table - Tipos já relacionado com o campo descições");

        jLabel4.setText("Tipos Registrados");

        btnAdicionar.setText("Adicionar");
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        jLabel5.setText("Data Vencimento");

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 972, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(cmbTipos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(75, 75, 75))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAdicionar)
                                .addGap(36, 36, 36)
                                .addComponent(btnExcluir)
                                .addGap(41, 41, 41)
                                .addComponent(btnAtualizar)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbTipos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar)
                    .addComponent(btnExcluir)
                    .addComponent(btnAtualizar))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        setBounds(0, 0, 1032, 685);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
    // Configuração do botão "Adicionar"
    // Obtém a descrição e o tipo selecionado
    String descricao = txtDescricao.getText().trim();
    String tipoSelecionado = (String) cmbTipos.getSelectedItem();
    
    // Verifica se a descrição e o tipo estão preenchidos (data opcional)
    if (descricao.isEmpty() || tipoSelecionado == null) {
        JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos obrigatórios.");
        return;
    }

    // Obtém o ID do tipo selecionado
    int tipoId = getTipoId(tipoSelecionado);
    if (tipoId == 0) {
        JOptionPane.showMessageDialog(null, "Tipo selecionado não encontrado.");
        return;
    }

    // Obtém a data de vencimento, se disponível
    Date dataVencimento = jDateChooser1.getDate();

    // Chama o método para inserir a nova descrição com a data opcional
    inserirDescricao(descricao, tipoId, dataVencimento);

    // Limpa os campos após a inserção
    limparCampos();

    // Atualiza a tabela para mostrar a nova descrição
    popularTabela();
    
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        // Configuração do botão "Excluir"
    int linhaSelecionada = tblDescricoes.getSelectedRow();
    
    if (linhaSelecionada < 0) {
        JOptionPane.showMessageDialog(null, "Selecione uma descrição para excluir.");
        return;
    }

    // Obtém o ID da descrição da tabela
    int descricaoId = (int) tblDescricoes.getValueAt(linhaSelecionada, 0); // ID da descrição
    String descricao = (String) tblDescricoes.getValueAt(linhaSelecionada, 1); // Descrição

    // Confirma a exclusão
    int resposta = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja excluir: " + descricao + "?", "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);
    if (resposta == JOptionPane.YES_OPTION) {
        excluirDescricao(descricaoId); // Método para excluir descrição
        popularTabela(); // Atualiza a tabela após exclusão
        limparCampos(); // Limpa os campos após a exclusão
    }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
     // Configuração do botão "Atualizar"
    int linhaSelecionada = tblDescricoes.getSelectedRow();
    
    if (linhaSelecionada < 0) {
        JOptionPane.showMessageDialog(null, "Selecione uma descrição para atualizar.");
        return;
    }

    // Obtém os dados da tabela
    int descricaoId = obterIdDescricao((String) tblDescricoes.getValueAt(linhaSelecionada, 1)); // Obtém o ID da descrição
    String novaDescricao = txtDescricao.getText(); // Nova descrição
    int novoTipoId = getTipoId((String) cmbTipos.getSelectedItem()); // Novo tipo
    Date novaDataVencimento = jDateChooser1.getDate(); // Nova data de vencimento

    if (novaDescricao.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios.");
        return;
    }

    // Atualiza a descrição com ou sem data
    if (novaDataVencimento != null) {
        atualizarDescricao(descricaoId, novaDescricao, novoTipoId, novaDataVencimento);
    } else {
        // Atualiza a descrição sem a data de vencimento
        atualizarDescricao(descricaoId, novaDescricao, novoTipoId, null);
    }

    popularTabela(); // Atualiza a tabela após a atualização
    limparCampos(); // Limpa os campos após a atualização
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void tblDescricoesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDescricoesMouseClicked
        // TODO add your handling code here:
    int linhaSelecionada = tblDescricoes.getSelectedRow();
    
    if (linhaSelecionada >= 0) {
        // Pega os valores da linha selecionada
        String descricao = (String) tblDescricoes.getValueAt(linhaSelecionada, 1); // Descrição
        int tipoId = (int) tblDescricoes.getValueAt(linhaSelecionada, 2); // Tipo ID
        Date dataVencimento = (Date) tblDescricoes.getValueAt(linhaSelecionada, 4); // Data de vencimento

        // Preenche os campos
        txtDescricao.setText(descricao); // Preenche a descrição
        cmbTipos.setSelectedItem(getTipoNome(tipoId)); // Preenche o tipo usando o nome
        jDateChooser1.setDate(dataVencimento); // Preenche a data de vencimento
    }
    }//GEN-LAST:event_tblDescricoesMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JComboBox<String> cmbTipos;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDescricoes;
    private javax.swing.JTextField txtDescricao;
    // End of variables declaration//GEN-END:variables
}
