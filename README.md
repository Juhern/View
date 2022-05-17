# Android Studio实现文字识别

## 调用手机相机/相册

相关代码注释已经贴在文章中，直接用即可



## 调用百度API

注意要根据文章的第二部分实现

**项目的包名一定要与你之前在百度申请的文字识别包名一致，血的教训！如果出错，记得检查manifest里面是否配置权限！相机进行文字识别需要先将图像保存后方可进行，不然会找不到图片的路径。**



#### Tips

- 新版本代码解决了toast导致的内存泄漏问题，改用handler传递消息；

- android 11版本以上的因为文件存储的方式相对之前的版本有所不同，所以导致【文字识别中拿不到图片在手机中的存储位置以及生成的文档不能存储到手机中】，这些也可能导致闪退，所以需要重写方法（即filelog类）