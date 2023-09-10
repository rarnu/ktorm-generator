import com.isyscore.kotlin.swing.dsl.*
import com.isyscore.kotlin.swing.errorMessageBox
import com.isyscore.kotlin.swing.messageBox
import com.isyscore.kotlin.swing.showDirectoryDialog
import java.awt.FlowLayout.LEFT
import java.sql.Connection
import javax.swing.*

class FormMain: JFrame("Ktorm Code Generator") {

    private val emptyBoard = BorderFactory.createEmptyBorder(2,4,2,4)

    private lateinit var cbDatabaseType: JComboBox<String>
    private lateinit var txtJdbcUrl: JTextField
    private lateinit var txtUser: JTextField
    private lateinit var txtPassword: JTextField
    private lateinit var txtPkgName: JTextField
    private lateinit var btnExportCode: JButton
    private lateinit var connection: Connection

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        contentVertPanel {
            horzPanel(align = LEFT) {
                border = emptyBoard
                preferredSize = 0 x 32
                label("数据库类型") {
                    preferredSize = 80 x 28
                }
                cbDatabaseType = combobox(array = arrayOf("MySQL", "Oracle", "SQL Server", "PostgreSQL", "SQLite", "达梦数据库")) {
                    preferredSize = 150 x 28
                    selectedIndex = 0
                }
            }
            horzPanel(align = LEFT) {
                border = emptyBoard
                preferredSize = 0 x 32
                label("JDBC URL") {
                    preferredSize = 80 x 28
                }
                txtJdbcUrl = input {
                    preferredSize = 300 x 28
                    text = "jdbc:mysql://localhost:3306/YugiohAPI2"
                }
            }
            horzPanel(align = LEFT) {
                border = emptyBoard
                preferredSize = 0 x 32
                label("用户名") {
                    preferredSize = 80 x 28
                }
                txtUser = input {
                    preferredSize = 300 x 28
                    text = "root"
                }
            }
            horzPanel(align = LEFT) {
                border = emptyBoard
                preferredSize = 0 x 32
                label("密码") {
                    preferredSize = 80 x 28
                }
                txtPassword = input {
                    preferredSize = 300 x 28
                    text = "rootroot"
                }
            }
            horzPanel(align = LEFT) {
                border = emptyBoard
                preferredSize = 0 x 32
                label("导出包名") {
                    preferredSize = 80 x 28
                }
                txtPkgName = input {
                    preferredSize = 300 x 28
                    text = "com.rarnu.yugioh.db"
                }
            }
            horzPanel(align = LEFT) {
                border = BorderFactory.createEmptyBorder(2, 84, 2, 32)
                button("连接数据库") {
                    preferredSize = 120 x 36
                    addActionListener {
                        connectDatabase()
                    }
                }
                btnExportCode = button("导出代码") {
                    preferredSize = 120 x 36
                    isEnabled = false
                    addActionListener {
                        exportCode()
                    }
                }
            }
        }
        size = 420 x 300
        minimumSize = 420 x 300
        isResizable = false
        setLocationRelativeTo(null)
        isVisible = true
    }


    private fun connectDatabase() {
        val driverType = cbDatabaseType.selectedIndex
        val jdbcUrl = txtJdbcUrl.text
        val user = txtUser.text
        val password = txtPassword.text
        if (jdbcUrl.isEmpty()) return
        Database.connect(driverType, jdbcUrl, user, password) { succ, conn, err ->
            if (succ) {
                connection = conn!!
                btnExportCode.isEnabled = true
            } else {
                errorMessageBox("错误", "连接数据库失败，原因为: $err 。")
            }
        }
    }

    private fun exportCode() {
        val pkgName = txtPkgName.text
        if (pkgName.isEmpty()) return
        showDirectoryDialog { dir ->
            Database.exportCode(pkgName, connection, dir) { total, exported ->
                messageBox("导出代码", "总计 $total 个表，已导出 $exported 个表。")
            }
        }
    }
}