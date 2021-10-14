# 枚举都不混淆，
-keep enum ** { *; }

# 导入导出的选项枚举不混淆，要作为文件名，
-keepclassmembers class cc.aoeiuv020.panovel.export.ExportOption { <fields>; }

# 本地持久化用到的实体类不能混淆，类名，方法名，变量名，都不能混淆，
-keep class cc.aoeiuv020.panovel.data.entity.** { *; }

# 和服务器端交互用的pojo需要gson序列化，不混淆，
-keepclassmembers class cc.aoeiuv020.panovel.server.dal.model.** { <fields>; }
-keepclassmembers class cc.aoeiuv020.panovel.server.ServerAddress { <fields>; }


# 需要用gson序列化的枚举不混淆，
## NovelChapter有在缓存库中保存章节列表，是直接gson序列化的，混淆的话，改了混淆结果会读出空，
-keepclassmembers class cc.aoeiuv020.panovel.api.NovelChapter { <fields>; }
-keepclassmembers class cc.aoeiuv020.reader.AnimationMode { <fields>; }
-keepclassmembers class cc.aoeiuv020.panovel.share.Expiration { <fields>; }

-keepclassmembers class cc.aoeiuv020.panovel.find.shuju.post.Post { *; }
-keepclassmembers class cc.aoeiuv020.panovel.find.shuju.list.Item { *; }


#jsoup https://stackoverflow.com/a/32169975/5615186
-keeppackagenames org.jsoup.nodes


#slf4j https://github.com/getsentry/sentry-java/issues/373
-dontwarn org.slf4j.**

# OkHttp https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-square-okhttp3.pro
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


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

# epublib, https://stackoverflow.com/a/33286911/5615186
-dontwarn org.kobjects.**
-dontwarn org.ksoap2.**
-dontwarn org.kxml2.**
-dontwarn org.xmlpull.v1.**
-keep class org.kobjects.** { *; }
-keep class org.ksoap2.** { *; }
-keep class org.kxml2.** { *; }
-keep class org.xmlpull.** { *; }
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontoptimize
-dontpreverify

# rhino, https://github.com/mozilla/rhino/issues/388
-keep class org.mozilla.** { *; }


#未混淆的类和成员
-printseeds build/seeds.txt
#列出从 apk 中删除的代码
-printusage build/unused.txt
#混淆前后的映射
-printmapping build/mapping.txt
# 保留行号，区分混淆后的同名方法，虽然会被inline影响，
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute ''
# 不同类的成员用不同名字，同一个类还是会用相同名字，
-useuniqueclassmembernames

#各种问题通通无视
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
