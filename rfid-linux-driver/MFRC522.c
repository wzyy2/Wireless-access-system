/*
 * �� �� ����MFRC522.c
 * о    Ƭ��STC12C5A60S2
 * ��    ��12MHz
 * �� �� �ߣ�����
 * �������ڣ�2010.11.12
 * �� �� �ߣ�
 * �޸����ڣ�
 * ����������Mifare1 Ѱ��������ͻ��ѡ������д ʵ��
 */

#include "MFRC522.h"
#include "SPI.h"
#include "Delay.h"
#include "UART.h"


#define	uchar	unsigned char
#define	uint	unsigned int


//4�ֽڿ����кţ���5�ֽ�ΪУ���ֽ�
uchar serNum[5];


//����ԭ������
void SetBitMask(uchar reg, uchar mask);
void ClearBitMask(uchar reg, uchar mask);
void AntennaOn(void);
void AntennaOff(void);


/*
 * �� �� ����Write_MFRC5200
 * ������������MFRC522��ĳһ�Ĵ���дһ���ֽ�����
 * ���������addr--�Ĵ�����ַ��val--Ҫд���ֵ
 * �� �� ֵ����
 */
void Write_MFRC522(uchar addr, uchar val)
{
	CS = 0;

	//��ַ��ʽ��0XXXXXX0
	SPI_Write((addr<<1)&0x7E);	
	SPI_Write(val);
	
	CS = 1;	
}


/*
 * �� �� ����Read_MFRC522
 * ������������MFRC522��ĳһ�Ĵ�����һ���ֽ�����
 * ���������addr--�Ĵ�����ַ
 * �� �� ֵ�����ض�ȡ����һ���ֽ�����
 */
uchar Read_MFRC522(uchar addr)
{
	uchar val;

	CS = 0;

	//��ַ��ʽ��1XXXXXX0
	SPI_Write(((addr<<1)&0x7E) | 0x80);	
	val = SPI_Read();
	
	CS = 1;
	
	return val;	
}

/*
 * �� �� ����SetBitMask
 * ������������RC522�Ĵ���λ
 * ���������reg--�Ĵ�����ַ;mask--��λֵ
 * �� �� ֵ����
 */
void SetBitMask(uchar reg, uchar mask)  
{
    uchar tmp;
    tmp = Read_MFRC522(reg);
    Write_MFRC522(reg, tmp | mask);  // set bit mask
}


/*
 * �� �� ����ClearBitMask
 * ������������RC522�Ĵ���λ
 * ���������reg--�Ĵ�����ַ;mask--��λֵ
 * �� �� ֵ����
 */
void ClearBitMask(uchar reg, uchar mask)  
{
    uchar tmp;
    tmp = Read_MFRC522(reg);
    Write_MFRC522(reg, tmp & (~mask));  // clear bit mask
} 


/*
 * �� �� ����AntennaOn
 * ������������������,ÿ��������ر����շ���֮��Ӧ������1ms�ļ��
 * �����������
 * �� �� ֵ����
 */
void AntennaOn(void)
{
	uchar temp;

	temp = Read_MFRC522(TxControlReg);
	if (!(temp & 0x03))
	{
		SetBitMask(TxControlReg, 0x03);
	}
}


/*
 * �� �� ����AntennaOff
 * �����������ر�����,ÿ��������ر����շ���֮��Ӧ������1ms�ļ��
 * �����������
 * �� �� ֵ����
 */
void AntennaOff(void)
{
	ClearBitMask(TxControlReg, 0x03);
}


/*
 * �� �� ����ResetMFRC522
 * ������������λRC522
 * �����������
 * �� �� ֵ����
 */
void MFRC522_Reset(void)
{
    Write_MFRC522(CommandReg, PCD_RESETPHASE);
}


/*
 * �� �� ����InitMFRC522
 * ������������ʼ��RC522
 * �����������
 * �� �� ֵ����
 */
void MFRC522_Init(void)
{
	NRSTPD = 1;

	MFRC522_Reset();
	 	
	//Timer: TPrescaler*TreloadVal/6.78MHz = 24ms
    Write_MFRC522(TModeReg, 0x8D);		//Tauto=1; f(Timer) = 6.78MHz/TPreScaler
    Write_MFRC522(TPrescalerReg, 0x3E);	//TModeReg[3..0] + TPrescalerReg
    Write_MFRC522(TReloadRegL, 30);           
    Write_MFRC522(TReloadRegH, 0);
	
	Write_MFRC522(TxAutoReg, 0x40);		//100%ASK
	Write_MFRC522(ModeReg, 0x3D);		//CRC��ʼֵ0x6363	???

	//ClearBitMask(Status2Reg, 0x08);		//MFCrypto1On=0
	//Write_MFRC522(RxSelReg, 0x86);		//RxWait = RxSelReg[5..0]
	//Write_MFRC522(RFCfgReg, 0x7F);   		//RxGain = 48dB

	AntennaOn();		//������
}


/*
 * �� �� ����MFRC522_Request
 * ����������Ѱ������ȡ�����ͺ�
 * ���������reqMode--Ѱ����ʽ��
 *			 TagType--���ؿ�Ƭ����
 *			 	0x4400 = Mifare_UltraLight
 *				0x0400 = Mifare_One(S50)
 *				0x0200 = Mifare_One(S70)
 *				0x0800 = Mifare_Pro(X)
 *				0x4403 = Mifare_DESFire
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_Request(uchar reqMode, uchar *TagType)
{
	uchar status;  
	uint backBits;			//���յ�������λ��

	Write_MFRC522(BitFramingReg, 0x07);		//TxLastBists = BitFramingReg[2..0]	???
	
	TagType[0] = reqMode;
	status = MFRC522_ToCard(PCD_TRANSCEIVE, TagType, 1, TagType, &backBits);

	if ((status != MI_OK) || (backBits != 0x10))
	{    
		status = MI_ERR;
	}
   
	return status;
}


/*
 * �� �� ����MFRC522_ToCard
 * ����������RC522��ISO14443��ͨѶ
 * ���������command--MF522�����֣�
 *			 sendData--ͨ��RC522���͵���Ƭ������, 
 *			 sendLen--���͵����ݳ���		 
 *			 backData--���յ��Ŀ�Ƭ�������ݣ�
 *			 backLen--�������ݵ�λ����
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_ToCard(uchar command, uchar *sendData, uchar sendLen, uchar *backData, uint *backLen)
{
    uchar status = MI_ERR;
    uchar irqEn = 0x00;
    uchar waitIRq = 0x00;
    uchar lastBits;
    uchar n;
    uint i;

    switch (command)
    {
        case PCD_AUTHENT:		//��֤����
		{
			irqEn = 0x12;
			waitIRq = 0x10;
			break;
		}
		case PCD_TRANSCEIVE:	//����FIFO������
		{
			irqEn = 0x77;
			waitIRq = 0x30;
			break;
		}
		default:
			break;
    }
   
    Write_MFRC522(CommIEnReg, irqEn|0x80);	//�����ж�����
    ClearBitMask(CommIrqReg, 0x80);			//��������ж�����λ
    SetBitMask(FIFOLevelReg, 0x80);			//FlushBuffer=1, FIFO��ʼ��
    
	Write_MFRC522(CommandReg, PCD_IDLE);	//NO action;ȡ����ǰ����	???

	//��FIFO��д������
    for (i=0; i<sendLen; i++)
    {   
		Write_MFRC522(FIFODataReg, sendData[i]);    
	}

	//ִ������
	Write_MFRC522(CommandReg, command);
    if (command == PCD_TRANSCEIVE)
    {    
		SetBitMask(BitFramingReg, 0x80);		//StartSend=1,transmission of data starts  
	}   
    
	//�ȴ������������
	i = 2000;	//i����ʱ��Ƶ�ʵ���������M1�����ȴ�ʱ��25ms	???
    do 
    {
		//CommIrqReg[7..0]
		//Set1 TxIRq RxIRq IdleIRq HiAlerIRq LoAlertIRq ErrIRq TimerIRq
        n = Read_MFRC522(CommIrqReg);
        i--;
    }
    while ((i!=0) && !(n&0x01) && !(n&waitIRq));

    ClearBitMask(BitFramingReg, 0x80);			//StartSend=0
	
    if (i != 0)
    {    
        if(!(Read_MFRC522(ErrorReg) & 0x1B))	//BufferOvfl Collerr CRCErr ProtecolErr
        {
            status = MI_OK;
            if (n & irqEn & 0x01)
            {   
				status = MI_NOTAGERR;			//??   
			}

            if (command == PCD_TRANSCEIVE)
            {
               	n = Read_MFRC522(FIFOLevelReg);
              	lastBits = Read_MFRC522(ControlReg) & 0x07;
                if (lastBits)
                {   
					*backLen = (n-1)*8 + lastBits;   
				}
                else
                {   
					*backLen = n*8;   
				}

                if (n == 0)
                {   
					n = 1;    
				}
                if (n > MAX_LEN)
                {   
					n = MAX_LEN;   
				}
				
				//��ȡFIFO�н��յ�������
                for (i=0; i<n; i++)
                {   
					backData[i] = Read_MFRC522(FIFODataReg);    
				}
            }
        }
        else
        {   
			status = MI_ERR;  
		}
        
    }
	
    //SetBitMask(ControlReg,0x80);           //timer stops
    //Write_MFRC522(CommandReg, PCD_IDLE); 

    return status;
}


/*
 * �� �� ����MFRC522_Anticoll
 * ��������������ͻ��⣬��ȡѡ�п�Ƭ�Ŀ����к�
 * ���������serNum--����4�ֽڿ����к�,��5�ֽ�ΪУ���ֽ�
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_Anticoll(uchar *serNum)
{
    uchar status;
    uchar i;
	uchar serNumCheck=0;
    uint unLen;
    

    //ClearBitMask(Status2Reg, 0x08);		//TempSensclear
    //ClearBitMask(CollReg,0x80);			//ValuesAfterColl
	Write_MFRC522(BitFramingReg, 0x00);		//TxLastBists = BitFramingReg[2..0]
 
    serNum[0] = PICC_ANTICOLL;
    serNum[1] = 0x20;
    status = MFRC522_ToCard(PCD_TRANSCEIVE, serNum, 2, serNum, &unLen);

    if (status == MI_OK)
	{
		//У�鿨���к�
		for (i=0; i<4; i++)
		{   
		 	serNumCheck ^= serNum[i];
		}
		if (serNumCheck != serNum[i])
		{   
			status = MI_ERR;    
		}
    }

    //SetBitMask(CollReg, 0x80);		//ValuesAfterColl=1

    return status;
} 


/*
 * �� �� ����CalulateCRC
 * ������������MF522����CRC
 * ���������pIndata--Ҫ����CRC�����ݣ�len--���ݳ��ȣ�pOutData--�����CRC���
 * �� �� ֵ����
 */
void CalulateCRC(uchar *pIndata, uchar len, uchar *pOutData)
{
    uchar i, n;

    ClearBitMask(DivIrqReg, 0x04);			//CRCIrq = 0
    SetBitMask(FIFOLevelReg, 0x80);			//��FIFOָ��
    //Write_MFRC522(CommandReg, PCD_IDLE);

	//��FIFO��д������	
    for (i=0; i<len; i++)
    {   
		Write_MFRC522(FIFODataReg, *(pIndata+i));   
	}
    Write_MFRC522(CommandReg, PCD_CALCCRC);

	//�ȴ�CRC�������
    i = 0xFF;
    do 
    {
        n = Read_MFRC522(DivIrqReg);
        i--;
    }
    while ((i!=0) && !(n&0x04));			//CRCIrq = 1

	//��ȡCRC������
    pOutData[0] = Read_MFRC522(CRCResultRegL);
    pOutData[1] = Read_MFRC522(CRCResultRegM);
}


/*
 * �� �� ����MFRC522_SelectTag
 * ����������ѡ������ȡ���洢������
 * ���������serNum--���뿨���к�
 * �� �� ֵ���ɹ����ؿ�����
 */
uchar MFRC522_SelectTag(uchar *serNum)
{
    uchar i;
	uchar status;
	uchar size;
    uint recvBits;
    uchar buffer[9]; 

	//ClearBitMask(Status2Reg, 0x08);			//MFCrypto1On=0

    buffer[0] = PICC_SElECTTAG;
    buffer[1] = 0x70;
    for (i=0; i<5; i++)
    {
    	buffer[i+2] = *(serNum+i);
    }
	CalulateCRC(buffer, 7, &buffer[7]);		//??
    status = MFRC522_ToCard(PCD_TRANSCEIVE, buffer, 9, buffer, &recvBits);
    
    if ((status == MI_OK) && (recvBits == 0x18))
    {   
		size = buffer[0]; 
	}
    else
    {   
		size = 0;    
	}

    return size;
}


/*
 * �� �� ����MFRC522_Auth
 * ������������֤��Ƭ����
 * ���������authMode--������֤ģʽ
                 0x60 = ��֤A��Կ
                 0x61 = ��֤B��Կ 
             BlockAddr--���ַ
             Sectorkey--��������
             serNum--��Ƭ���кţ�4�ֽ�
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_Auth(uchar authMode, uchar BlockAddr, uchar *Sectorkey, uchar *serNum)
{
    uchar status;
    uint recvBits;
    uchar i;
	uchar buff[12]; 

	//��ָ֤��+���ַ���������룫�����к�
    buff[0] = authMode;
    buff[1] = BlockAddr;
    for (i=0; i<6; i++)
    {    
		buff[i+2] = *(Sectorkey+i);   
	}
    for (i=0; i<4; i++)
    {    
		buff[i+8] = *(serNum+i);   
	}
    status = MFRC522_ToCard(PCD_AUTHENT, buff, 12, buff, &recvBits);

    if ((status != MI_OK) || (!(Read_MFRC522(Status2Reg) & 0x08)))
    {   
		status = MI_ERR;   
	}
    
    return status;
}


/*
 * �� �� ����MFRC522_Read
 * ������������������
 * ���������blockAddr--���ַ;recvData--�����Ŀ�����
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_Read(uchar blockAddr, uchar *recvData)
{
    uchar status;
    uint unLen;

    recvData[0] = PICC_READ;
    recvData[1] = blockAddr;
    CalulateCRC(recvData,2, &recvData[2]);
    status = MFRC522_ToCard(PCD_TRANSCEIVE, recvData, 4, recvData, &unLen);

    if ((status != MI_OK) || (unLen != 0x90))
    {
        status = MI_ERR;
    }
    
    return status;
}


/*
 * �� �� ����MFRC522_Write
 * ����������д������
 * ���������blockAddr--���ַ;writeData--���д16�ֽ�����
 * �� �� ֵ���ɹ�����MI_OK
 */
uchar MFRC522_Write(uchar blockAddr, uchar *writeData)
{
    uchar status;
    uint recvBits;
    uchar i;
	uchar buff[18]; 
    
    buff[0] = PICC_WRITE;
    buff[1] = blockAddr;
    CalulateCRC(buff, 2, &buff[2]);
    status = MFRC522_ToCard(PCD_TRANSCEIVE, buff, 4, buff, &recvBits);

    if ((status != MI_OK) || (recvBits != 4) || ((buff[0] & 0x0F) != 0x0A))
    {   
		status = MI_ERR;   
	}
        
    if (status == MI_OK)
    {
        for (i=0; i<16; i++)		//��FIFOд16Byte����
        {    
        	buff[i] = *(writeData+i);   
        }
        CalulateCRC(buff, 16, &buff[16]);
        status = MFRC522_ToCard(PCD_TRANSCEIVE, buff, 18, buff, &recvBits);
        
		if ((status != MI_OK) || (recvBits != 4) || ((buff[0] & 0x0F) != 0x0A))
        {   
			status = MI_ERR;   
		}
    }
    
    return status;
}


/*
 * �� �� ����MFRC522_Halt
 * �������������Ƭ��������״̬
 * �����������
 * �� �� ֵ����
 */
void MFRC522_Halt(void)
{
	uchar status;
    uint unLen;
    uchar buff[4]; 

    buff[0] = PICC_HALT;
    buff[1] = 0;
    CalulateCRC(buff, 2, &buff[2]);
 
    status = MFRC522_ToCard(PCD_TRANSCEIVE, buff, 4, buff,&unLen);
}