/**
 * Twitter_Snowflake����demo��ʽ<br>
 * SnowFlake�Ľṹ����(ÿ������-�ֿ�):<br>
 * 1λ��־λ                    41λʱ���                                               5λ����+5λ���ݱ�־          12λ������                               
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 41λʱ�������Ϊ��ǰʱ���ȥ��ʼʱ���ʱ����պ���2^41��Χ��
 * 1λ��ʶ������long����������Java���Ǵ����ŵģ����λ�Ƿ���λ��������0��������1������idһ�������������λ��0<br>
 * 41λʱ���(���뼶)��ע�⣬41λʱ��ز��Ǵ洢��ǰʱ���ʱ��أ����Ǵ洢ʱ��صĲ�ֵ����ǰʱ��� - ��ʼʱ���)
 * �õ���ֵ��������ĵĿ�ʼʱ��أ�һ�������ǵ�id��������ʼʹ�õ�ʱ�䣬�����ǳ�����ָ���ģ������������IdWorker���startTime���ԣ���41λ��ʱ��أ�����ʹ��69�꣬��T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10λ�����ݻ���λ�����Բ�����1024���ڵ㣬����5λdatacenterId��5λworkerId<br>
 * 12λ���У������ڵļ�����12λ�ļ���˳���֧��ÿ���ڵ�ÿ����(ͬһ������ͬһʱ���)����4096��ID���<br>
 * �������պ�64λ��Ϊһ��Long�͡�<br>
 * SnowFlake���ŵ��ǣ������ϰ���ʱ���������򣬲��������ֲ�ʽϵͳ�ڲ������ID��ײ(����������ID�ͻ���ID������)������Ч�ʽϸߣ������ԣ�SnowFlakeÿ���ܹ�����26��ID���ҡ�
 * 
 * �㷨�ڵ�<<��ʾ���ƣ����ƺ����ʣ�²��ֻᲹ0��������ѭ����λ
 * ������ת�������и����Ķ�������Ҫ�ò����ʾ
 * ���� = ���� + 1 ;   ���� = ��ԭ�� - 1����ȡ����
 * 
 * ��������ֶν����˵���������43λʱ���  3λ������־��3λ���ݱ�־  14λ
 */
public class SnowFlakeIdWorker {

    // ==============================Fields===========================================
    /** ��ʼʱ��� (2018-01-01) ���뼶ʱ��� */
    private final long twepoch = 1514736000000L;

    /** ����id��ռ��λ�� */
    private final long workerIdBits = 3L;

    /** ���ݱ�ʶid��ռ��λ�� */
    private final long datacenterIdBits = 3L;

    /** 
     * ֧�ֵ�������id�������7 (�����λ�㷨���Ժܿ�ļ������λ�����������ܱ�ʾ�����ʮ������) 
     * -1L ^ (-1L << n)��ʾռn��bit�����ֵ����ֵ�Ƕ��١��ٸ����ӣ�-1L ^ (-1L << 2)����10���Ƶ�3 ���������Ƶ�11��ʾʮ����3��
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 
     * ֧�ֵ�������ݱ�ʶid�������7 
     *  -1L ԭ�� 1000 0001   ԭ�����ڷ���λ�ӱ�־  ���� 0 ����1
     *     ����1111 1110   ����ԭ�����䱾�� �����Ƿ���λ��������λȡ��
     *     ����1111 1111   ԭ��-1��ȡ��
     *  -1L << 3 1111 1000   
     *  -1L ^ (1111 1000)   1111 1000 ^ 1111 1111 ���Ϊ7
     * */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** ������id��ռ��λ�� */
    private final long sequenceBits = 14L;

    /** ����ID������14λ */
    private final long workerIdShift = sequenceBits;

    /** ���ݱ�ʶid������17λ(14+3) */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** ʱ���������20λ(3+3+14) ������+������ */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** �������е����룬����Ϊ16383  */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** ��������ID(0~7) */
    private long workerId;

    /** ��������ID(0~7) */
    private long datacenterId;

    /** ����������(0~16383) */
    private long sequence = 0L;

    /** �ϴ�����ID��ʱ��� */
    private long lastTimestamp = -1L;

    private static String wordid;
    private static String dataid;

    private static SnowFlakeIdWorker idWorker;

    //==============================Constructors=====================================
    /**
     * ���캯��
     * @param workerId ����ID (0~7)
     * @param datacenterId ��������ID (0~7)
     */
    public SnowFlakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * �ṩ�չ������ڴ���������÷��� 
     */
    public SnowFlakeIdWorker() {}

    // ==============================Methods==========================================
    /**
     * �����һ��ID (�÷������̰߳�ȫ��)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //�����ǰʱ��С����һ��ID���ɵ�ʱ�����˵��ϵͳʱ�ӻ��˹����ʱ��Ӧ���׳��쳣
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //�����ͬһʱ�����ɵģ�����к���������
        if (lastTimestamp == timestamp) {
            //���к� = �ϴ����к�+1 �� �������е�����
            sequence = (sequence + 1) & sequenceMask;
            //�������������
            if (sequence == 0) {
                //��������һ������,����µ�ʱ���
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //ʱ����ı䣬��������������
        else {
            sequence = 0L;
        }

        //�ϴ�����ID��ʱ��أ�����ʱ�����
        lastTimestamp = timestamp;

        //��λ��ͨ��������ƴ��һ�����64λ��ID(������)
        return ((timestamp - twepoch) << timestampLeftShift) //��ǰʱ���-��ʼʱ��� ����22λ�൱���������22λ
                | (datacenterId << datacenterIdShift) //��������Id����17λ
                | (workerId << workerIdShift) // ����id����12λ
                | sequence; //����������
    }

    /**
     * ��������һ�����룬ֱ������µ�ʱ���
     * @param lastTimestamp �ϴ�����ID��ʱ���
     * @return ��ǰʱ���
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * �����Ժ���Ϊ��λ�ĵ�ǰʱ��
     * @return ��ǰʱ��(����)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    //==============================Test=============================================
    /** ���� */
    public static void main(String[] args) {
        SnowFlakeIdWorker idWorker = new SnowFlakeIdWorker(1L, 1L);
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            System.out.println(Long.toBinaryString(id));
            System.out.println(id);
        }
    }

    /**
     * ��ȡ���ݿ��л��������Ϣ�����ض���
     * @return
     */
    public SnowFlakeIdWorker getSnowFlakeIdWorker(){
        //���������ļ�
        InputStream is = WeixinConfigUtils.class.getResourceAsStream("/snowflake.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BasicRuntimeException("����snowflake�ļ��쳣" + e.getMessage());
        }
        wordid = properties.getProperty("worid"); 
        dataid = properties.getProperty("dataid"); 
        //У���ȡ���������ڽ��м���
        if (StringUtils.isNotBlank(dataid) && StringUtils.isNotBlank(wordid)) {
            idWorker = new SnowFlakeIdWorker(Long.valueOf(dataid), Long.valueOf(dataid));
            return idWorker;
        }
        return null;
    }

    /**
     * ��ȡid
     */
    public Long getId(){

        SnowFlakeIdWorker snowFlakeIdWorker = getSnowFlakeIdWorker();
        if (snowFlakeIdWorker != null) {
            return snowFlakeIdWorker.nextId();
        }
        return null;
    }
}