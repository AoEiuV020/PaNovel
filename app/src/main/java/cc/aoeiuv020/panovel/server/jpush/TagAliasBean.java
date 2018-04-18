package cc.aoeiuv020.panovel.server.jpush;

import java.util.Set;

/**
 * 极光推送标签相关api调用时传入helper(TagAliasOperatorHelper),
 * 传递时有用到序列化，立刻消费，混淆也没问题，
 * Created by AoEiuV020 on 2018.04.17-12:41:23.
 */
@SuppressWarnings("all")
public class TagAliasBean {
    public int action;
    public Set<String> tags;
    public String alias;
    public boolean isAliasAction;

    @Override
    public String toString() {
        return "TagAliasBean{" +
                "action=" + action +
                ", tags=" + tags +
                ", alias='" + alias + '\'' +
                ", isAliasAction=" + isAliasAction +
                '}';
    }
}
