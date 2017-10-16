

# 需要用gson序列化的类成员不混淆，
-keepclassmembers class * extends cc.aoeiuv020.panovel.api.Data { <fields>; }
-keepclassmembers class * extends cc.aoeiuv020.panovel.api.Requester { <fields>; }
-keepclassmembers class * extends cc.aoeiuv020.panovel.local.LocalData { <fields>; }

# 本地保存用到类名，不混淆，
-keepnames class * implements cc.aoeiuv020.panovel.local.LocalSource


#jsoup https://stackoverflow.com/a/32169975/5615186
-keeppackagenames org.jsoup.nodes

#glide https://github.com/bumptech/glide#proguard
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule


#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt


#各种问题通通无视
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
