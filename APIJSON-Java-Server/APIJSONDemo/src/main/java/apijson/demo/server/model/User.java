package apijson.demo.server.model;

import java.util.List;

import com.zhangls.apijson.annotation.MethodAccess;
import com.zhangls.apijson.base.service.Visitor;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.UNKNOWN;

/**
 * 用户开放信息
 *
 * @author Lemon
 */
@MethodAccess(
        POST = {UNKNOWN, ADMIN},
        DELETE = {ADMIN}
)
public class User extends BaseModel implements Visitor<Long> {
    private static final long serialVersionUID = 1L;

    public static final int SEX_MAIL = 0;
    public static final int SEX_FEMALE = 1;
    public static final int SEX_UNKNOWN = 2;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像url
     */
    private String head;

    /**
     * 姓名
     */
    private String name;

    /**
     * 标签
     */
    private String tag;

    /**
     * 照片列表
     */
    private List<String> pictureList;

    /**
     * 朋友列表
     */
    private List<Long> contactIdList;

    /**
     * 默认构造方法，JSON等解析时必须要有
     */
    public User() {
        super();
    }

    public User(long id) {
        this();
        setId(id);
    }

    public Integer getSex() {
        return sex;
    }

    public User setSex(Integer sex) {
        this.sex = sex;
        return this;
    }

    public String getHead() {
        return head;
    }

    public User setHead(String head) {
        this.head = head;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getPictureList() {
        return pictureList;
    }

    public User setPictureList(List<String> pictureList) {
        this.pictureList = pictureList;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public User setTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public List<Long> getContactIdList() {
        return contactIdList;
    }

    public User setContactIdList(List<Long> contactIdList) {
        this.contactIdList = contactIdList;
        return this;
    }


}
