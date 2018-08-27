import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * 生成id的方法
 */
@Service
@Component
@Transactional
public class GenerateId implements IdentifierGenerator,Configurable{
    @Resource
    private SessionFactory sessionFactory;
    public String workid;

    public String dataid;

    public SnowFlakeIdWorker snowFlakeIdWorker;

    /**
     * hibernate自定义主键生成规则必须实现 IdentifierGenerator  generate 为默认方法
     */
    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {
        Long id = snowFlakeIdWorker.nextId();
        if (id != null) {
            return id;
        }else {
            return null;
        }
    }

    /**
     * 加载配置文件中的数据初始化snowflakeworker类
     */
    @Override
    public void configure(Type type, Properties properties, Dialect d)
            throws MappingException {
        //加载配置文件
        InputStream is = GenerateId.class.getResourceAsStream("/snowflake.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BasicRuntimeException(this,"加载snowflake文件异常" + e.getMessage());
        }
        workid = properties.getProperty("workid"); 
        dataid = properties.getProperty("dataid"); 
        if (StringUtils.isNotBlank(dataid) && StringUtils.isNotBlank(workid)) {
            snowFlakeIdWorker = new SnowFlakeIdWorker(Long.valueOf(workid), Long.valueOf(dataid));
        }
    }

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getDataid() {
        return dataid;
    }

    public void setDataid(String dataid) {
        this.dataid = dataid;
    }
}