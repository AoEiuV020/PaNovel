# 枚举都不混淆，
-keep enum ** { *; }

# 导入导出的选项枚举不混淆，要作为文件名，
-keepclassmembers class cc.aoeiuv020.panovel.export.ExportOption { <fields>; }

# 本地持久化用到的实体类不能混淆，
-keep class cc.aoeiuv020.panovel.data.entity.** { *; }

# 和服务器端交互用的pojo需要gson序列化，不混淆，
-keepclassmembers class cc.aoeiuv020.panovel.server.dal.model.** { <fields>; }
-keepclassmembers class cc.aoeiuv020.panovel.server.ServerAddress { <fields>; }


# 需要用gson序列化的类成员不混淆，
-keepclassmembers class * extends cc.aoeiuv020.panovel.api.Data { <fields>; }
-keepclassmembers class * extends cc.aoeiuv020.panovel.local.LocalData { <fields>; }
-keepclassmembers class cc.aoeiuv020.reader.AnimationMode { <fields>; }
-keepclassmembers class cc.aoeiuv020.panovel.share.Expiration { <fields>; }

# 本地保存用到类名，不混淆，
-keepnames class * implements cc.aoeiuv020.panovel.local.LocalSource
-keepnames class * extends cc.aoeiuv020.panovel.api.Data
-keepnames class * extends cc.aoeiuv020.panovel.api.Requester
-keepnames class * extends cc.aoeiuv020.panovel.local.LocalData

# 静态方法new不混淆，为了反射方法new代替初始化，
-keepclassmembers class * extends cc.aoeiuv020.panovel.api.Requester { public static final *** new(java.lang.String); }


#jsoup https://stackoverflow.com/a/32169975/5615186
-keeppackagenames org.jsoup.nodes


#slf4j https://github.com/getsentry/sentry-java/issues/373
-dontwarn org.slf4j.**


#glide https://github.com/bumptech/glide#proguard
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#bugly https://bugly.qq.com/docs/user-guide/instruction-manual-android/?v=20170912151050#_5
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# firebase
-keepattributes Signature
-keepattributes *Annotation*
# admob https://github.com/googleads/googleads-mobile-android-examples/blob/master/java/admob/BannerExample/app/proguard-rules.pro
# For Google Play Services
-keep public class com.google.android.gms.ads.**{
   public *;
}
# For old ads classes
-keep public class com.google.ads.**{
   public *;
}
# Other required classes for Google Play Services
# Read more at http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
   protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}

# jpush, https://docs.jiguang.cn/jpush/client/Android/android_guide/
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }


#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt
# 保留行号，区分混淆后的同名方法，
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute ''
# 不同类的成员用不同名字，同一个类还是会用相同名字，
-useuniqueclassmembernames

#各种问题通通无视
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
