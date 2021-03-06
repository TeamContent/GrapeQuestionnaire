package interfaceApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import CommonModel.CModel;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef.UserMode;
import common.java.check.checkHelper;
import common.java.database.dbFilter;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.session.session;
import common.java.string.StringHelper;
import common.java.time.TimeHelper;
/**
 * 
 * 题目管理
 *
 */
public class QuestionInfo {
    private GrapeTreeDBModel QuestInfo;
    private GrapeDBSpecField gDbSpecField;
    private String pkString;
    private CModel model;

    private session se;
    private JSONObject userInfo = null;
    private String currentWeb = null;
    private int userType = 0;

    public QuestionInfo() {
        QuestInfo = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("QuestionInfo"));
        QuestInfo.descriptionModel(gDbSpecField);
        QuestInfo.bindApp();
        pkString = QuestInfo.getPk();

        model = new CModel();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            currentWeb = userInfo.getString("currentWeb");
            userType = userInfo.getInt("userType");
        }
    }

    /**
     * 新增题目信息
     * 
     * @param questionInfo
     * @return
     */
    @SuppressWarnings("unchecked")
    public String insert(String questionInfo) {
        String questionId = null;
        questionInfo = codec.DecodeFastJSON(questionInfo);
        JSONObject object = JSONObject.toJSON(questionInfo);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        object.put("createTime", TimeHelper.nowMillis());
        object.put("wbid", currentWeb);
        questionId = (String) QuestInfo.data(object).autoComplete().insertOnce();
        return get(questionId);
    }

    /**
     * 修改题目信息
     * 
     * @param questionId
     * @param questionInfo
     * @return
     */
    public String update(String questionId, String questionInfo) {
        String result = rMsg.netMSG(100, "修改失败");
        questionInfo = codec.DecodeFastJSON(questionInfo);
        if (!StringHelper.InvaildString(questionId)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        JSONObject object = JSONObject.toJSON(questionInfo);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        object = QuestInfo.eq(pkString, questionId).data(object).update();
        result = object != null ? rMsg.netMSG(0, "修改成功") : result;
        return result;
    }

    /**
     * 删除题目类型
     * 
     * @param questionIds
     * @return
     */
    public String delete(String questionIds) {
        String result = rMsg.netMSG(100, "删除失败");
        long code = 0;
        String[] value = null;
        if (!StringHelper.InvaildString(questionIds)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        value = questionIds.split(",");
        if (value != null) {
            JSONArray condArray = model.deleteBuildCond(pkString, value);
            QuestInfo.or().where(condArray);
            if (QuestInfo.getCondCount() > 0) {
                code = QuestInfo.deleteAll();
            }
        }
        return code > 0 ? rMsg.netMSG(0, "删除成功") : result;
    }

    /**
     * 分页获取题目类型
     * 
     * @param idx
     * @param pageSize
     * @return
     */
    public String page(int idx, int pageSize) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        if (userType > 0) {
            if (userType >= UserMode.admin && userType < UserMode.root) {
                QuestInfo.eq("wbid", currentWeb);
            }
            count = QuestInfo.count();
            array = QuestInfo.page(idx, pageSize);
        }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    /**
     * 根据条件分页获取题目信息
     * 
     * @param idx
     * @param pageSize
     * @param condString
     * @return
     */
    public String pageby(int idx, int pageSize, String condString) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }

        JSONArray condArray = model.searchBuildCond(condString);
        if (userType > 0) {
            if (userType >= UserMode.admin && userType < UserMode.root) {
                QuestInfo.eq("wbid", currentWeb);
            }
            if (condArray != null && condArray.size() > 0) {
                array = QuestInfo.where(condArray).page(idx, pageSize);
            } else {
                return rMsg.netMSG(false, "无效条件");
            }
        }
        // JSONArray condArray = JSONArray.toJSONArray(condString);
        // if (condArray != null && condArray.size() > 0) {
        // array = QuestInfo.where(condArray).page(idx, pageSize);
        // }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    /**
     * 获取一条题目信息数据
     * 
     * @param questionId
     * @return
     */
    public String get(String questionId) {
        JSONObject object = null;
        if (!StringHelper.InvaildString(questionId)) {
            return rMsg.netMSG(2, "无效类型id");
        }
        if (ObjectId.isValid(questionId) || checkHelper.isInt(questionId)) {
            object = QuestInfo.eq(pkString, questionId).find();
        }
        return rMsg.netMSG(true, (object != null && object.size() > 0) ? object : new JSONObject());
    }

    /**
     * 获取所有题目信息
     * 
     * @return
     */
    public String getAll() {
        return rMsg.netMSG(true, QuestInfo.select());
    }

    /**
     * 根据题目信息，获取指定数量的的题目信息
     * 
     * @param num
     * @param typeIds
     * @return
     */
    protected JSONObject getRandom(int num, String typeIds) {
        List<String> list = getQuestInfo(typeIds);
        String questID = getRandQuestID(num, list);
        JSONObject object = getQuestInfoById(questID);
        return object;
    }

    /**
     * 根据题目id获取题目信息
     * 
     * @param ids
     * @return
     */
    @SuppressWarnings("unchecked")
    protected JSONObject getQuestInfoById(String ids) {
        String tempID;
        String[] value = null;
        JSONArray array = null;
        JSONObject rjson = new JSONObject(), object;
        if (StringHelper.InvaildString(ids)) {
            value = ids.split(",");
            JSONArray condArray = model.deleteBuildCond(pkString, value);
            QuestInfo.or().where(condArray);
            if (QuestInfo.getCondCount() > 0) {
                array = QuestInfo.field("name,type,options,answer").select();
            }
            if (array != null && array.size() > 0) {
                for (Object obj : array) {
                    object = (JSONObject) obj;
                    tempID = object.getMongoID("_id");
                    rjson.put(tempID, object);
                }
            }
        }
        return rjson;
    }

    /**
     * 根据类型获取题目id
     * 
     * @param typeIds
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    private List<String> getQuestInfo(String typeIds) {
        String[] value = null;
        JSONArray array = null;
        dbFilter filter = new dbFilter();
        value = typeIds.split(",");
        if (value != null && value.length > 0) {
            for (String typeId : value) {
                if (StringHelper.InvaildString(typeId)) {
                    filter.eq("type", Integer.parseInt(typeId));
                }
            }
        }
        JSONArray condArray = filter.build();
        if (condArray != null && condArray.size() > 0) {
            array = QuestInfo.or().where(condArray).field("_id").scan((_array) -> {
                JSONObject obj;
                String tempID = "";
                JSONArray tempArray = new JSONArray();
                if (_array != null && _array.size() > 0) {
                    for (Object object : condArray) {
                        obj = (JSONObject) object;
                        tempID = obj.getMongoID("_id");
                        if (StringHelper.InvaildString(tempID)) {
                            tempArray.add(tempID);
                        }
                    }
                }
                return tempArray;
            }, 50);
        }
        return model.jsonArray2List(array);
    }

    /**
     * 获取随机题目id
     * 
     * @param num
     * @param list
     * @return
     */
    private String getRandQuestID(int num, List<String> list) {
        Random r = new Random();
        List<String> rList = new ArrayList<>();
        if (!list.isEmpty()) {
            int size = list.size();
            if (size > 0) {
                if (size <= num) {
                    rList = list;
                } else {
                    int index = 0;
                    for (int i = 0; i < num; i++) {
                        index = r.nextInt(size - i);
                        rList.add(list.get(index));
                        list.remove(index);
                    }
                }
            }
        }
        return StringHelper.join(rList);
    }
}
