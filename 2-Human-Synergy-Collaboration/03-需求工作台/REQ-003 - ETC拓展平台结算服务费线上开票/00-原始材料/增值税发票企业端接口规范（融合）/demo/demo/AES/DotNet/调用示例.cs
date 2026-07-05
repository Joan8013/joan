using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Security.Cryptography;

namespace MyAES
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
        }

        private void button1_Click(object sender, EventArgs e)
        {
            String strTest = "中文啊LyoqIAoJICog5Yqg5a+GIAoJICogIAoJICogQHBhcmFtIGNvbnRlbnQg6ZyA6KaB5Yqg5a+G55qE5YaF5a65IAoJICogQHBhcmFtIHBhc3N3b3JkICDliqDlr4blr4bnoIEgCgkgKiBAcmV0dXJuIAoJICovICAKCXB1YmxpYyBzdGF0aWMgYnl0ZVtdIGVuY3J5cHQoU3RyaW5nIGNvbnRlbnQsIFN0cmluZyBwYXNzd29yZCkgeyAgCgkgICAgICAgIHRyeSB7ICAgICAgICAgICAgIAoJICAgICAgICAgICAgICAgIEtleUdlbmVyYXRvciBrZ2VuID0gS2V5R2VuZXJhdG9yLmdldEluc3RhbmNlKCJBRVMiKTsgIAoJICAgICAgICAgICAgICAgIGtnZW4uaW5pdCgxMjgsIG5ldyBTZWN1cmVSYW5kb20ocGFzc3dvcmQuZ2V0Qnl0ZXMoKSkpOyAgCgkgICAgICAgICAgICAgICAgU2VjcmV0S2V5IHNlY3JldEtleSA9IGtnZW4uZ2VuZXJhdGVLZXkoKTsgIAoJICAgICAgICAgICAgICAgIGJ5dGVbXSBlbkNvZGVGb3JtYXQgPSBzZWNyZXRLZXkuZ2V0RW5jb2RlZCgpOyAgCgkgICAgICAgICAgICAgICAgU2VjcmV0S2V5U3BlYyBrZXkgPSBuZXcgU2VjcmV0S2V5U3BlYyhlbkNvZGVGb3JtYXQsICJBRVMiKTsgIAoJICAgICAgICAgICAgICAgIENpcGhlciBjaXBoZXIgPSBDaXBoZXIuZ2V0SW5zdGFuY2UoIkFFUyIpOy8vIOWIm+W7uuWvhueggeWZqCAgCgkgICAgICAgICAgICAgICAgYnl0ZVtdIGJ5dGVDb250ZW50ID0gY29udGVudC5nZXRCeXRlcygidXRmLTgiKTsgIAoJICAgICAgICAgICAgICAgIGNpcGhlci5pbml0KENpcGhlci5FTkNSWVBUX01PREUsIGtleSk7Ly8g5Yid5aeL5YyWICAKCSAgICAgICAgICAgICAgICBieXRlW10gcmVzdWx0ID0gY2lwaGVyLmRvRmluYWwoYnl0ZUNvbnRlbnQpOyAgCgkgICAgICAgICAgICAgICAgcmV0dXJuIHJlc3VsdDsgLy8g5Yqg5a+GICAKCSAgICAgICAgfSAgY2F0Y2ggKEV4Y2VwdGlvbiBlKSB7ICAKCSAgICAgICAgICAgICAgICBlLnByaW50U3RhY2tUcmFjZSgpOyAgCgkgICAgICAgIH0gIAoJICAgICAgICByZXR1cm4gbnVsbDsgIAoJfSAgCgkKCS8qKuino+WvhiAKCSAqIEBwYXJhbSBjb250ZW50ICDlvoXop6Plr4blhoXlrrkgCgkgKiBAcGFyYW0gcGFzc3dvcmQg6Kej5a+G5a+G6ZKlIAoJICogQHJldHVybiAKCSAqLyAgCglwdWJsaWMgc3RhdGljIGJ5dGVbXSBkZWNyeXB0KGJ5dGVbXSBjb250ZW50LCBTdHJpbmcgcGFzc3dvcmQpIHsgIAoJICAgICAgICB0cnkgeyAgCgkgICAgICAgICAgICAgICAgIEtleUdlbmVyYXRvciBrZ2VuID0gS2V5R2VuZXJhdG9yLmdldEluc3RhbmNlKCJBRVMiKTsgIAoJICAgICAgICAgICAgICAgICBrZ2VuLmluaXQoMTI4LCBuZXcgU2VjdXJlUmFuZG9tKHBhc3N3b3JkLmdldEJ5dGVzKCkpKTsgIAoJICAgICAgICAgICAgICAgICBTZWNyZXRLZXkgc2VjcmV0S2V5ID0ga2dlbi5nZW5lcmF0ZUtleSgpOyAgCgkgICAgICAgICAgICAgICAgIGJ5dGVbXSBlbkNvZGVGb3JtYXQgPSBzZWNyZXRLZXkuZ2V0RW5jb2RlZCgpOyAgCgkgICAgICAgICAgICAgICAgIFNlY3JldEtleVNwZWMga2V5ID0gbmV3IFNlY3JldEtleVNwZWMoZW5Db2RlRm9ybWF0LCAiQUVTIik7ICAgICAgICAgICAgICAKCSAgICAgICAgICAgICAgICAgQ2lwaGVyIGNpcGhlciA9IENpcGhlci5nZXRJbnN0YW5jZSgiQUVTIik7Ly8g5Yib5bu65a+G56CB5ZmoICAKCSAgICAgICAgICAgICAgICBjaXBoZXIuaW5pdChDaXBoZXIuREVDUllQVF9NT0RFLCBrZXkpOy8vIOWIneWni+WMliAgCgkgICAgICAgICAgICAgICAgYnl0ZVtdIHJlc3VsdCA9IGNpcGhlci5kb0ZpbmFsKGNvbnRlbnQpOyAgCgkgICAgICAgICAgICAgICAgcmV0dXJuIHJlc3VsdDsgLy8g5Yqg5a+GICAKCSAgICAgICAgfSBjYXRjaCAoRXhjZXB0aW9uIGUpIHsgIAoJICAgICAgICAgICAgICAgIGUucHJpbnRTdGFja1RyYWNlKCk7ICAKCSAgICAgICAgfSAgCgkgICAgICAgIHJldHVybiBudWxsOyAgCgl9IA==";
            String strPassword = "1234567890123456";
            String strPassword2 = "abcdefghijklmnop";

            String strHash = MyAES.GetMD5(strTest);
            Console.WriteLine("Hash=" + strHash + "(" + strTest.Length.ToString() + ")");

            for (int i = 0; i < 100000; i++)
            {
                DateTime dt1 = DateTime.Now;
                String strPwd;
                if (i % 2 == 0)
                    strPwd = strPassword;
                else
                    strPwd = strPassword2;

                Console.WriteLine("****************.Net 第" + (i + 1).ToString() + "次测试***********************");

                String strResult = MyAES.Encrypt(strHash, strPwd);
                Console.WriteLine("密文=" + strResult);

                DateTime dt2 = DateTime.Now;
                TimeSpan span = dt2 - dt1;
                Console.WriteLine("加密=" + span.TotalMilliseconds.ToString() + "毫秒");

                String strText = MyAES.Decrypt(strResult, strPwd);
                Console.WriteLine("原文=" + strText);

                TimeSpan span2 = DateTime.Now - dt2;
                Console.WriteLine("解密=" + span2.TotalMilliseconds.ToString() + "毫秒");
            }
        }

    }
}
