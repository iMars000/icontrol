import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * ����id�ķ���
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
     * hibernate�Զ����������ɹ������ʵ�� IdentifierGenerator  generate ΪĬ�Ϸ���
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
     * ���������ļ��е����ݳ�ʼ��snowflakeworker��
     */
    @Override
    public void configure(Type type, Properties properties, Dialect d)
            throws MappingException {
        //���������ļ�
        InputStream is = GenerateId.class.getResourceAsStream("/snowflake.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BasicRuntimeException(this,"����snowflake�ļ��쳣" + e.getMessage());
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