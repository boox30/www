#ifndef DRMALGORITHM_H_INCLUDED
#define DRMALGORITHM_H_INCLUDED

typedef void* Cipher;
typedef Cipher* PCipher;
typedef void* Digestor;
typedef Cipher* PDigestor;
typedef void* CipherStream;
typedef CipherStream* PCipherStream;

#ifdef WIN32
typedef int (__cdecl* read_proc)(void* userp, void* buf, int buf_len);
typedef int (__cdecl* write_proc)(void* userp, void* buf, int buf_len);
#else
typedef int (__attribute__((__cdecl__))* read_proc)(void* userp, void* buf, int buf_len);
typedef int (__attribute__((__cdecl__))* write_proc)(void* userp, void* buf, int buf_len);
#endif

typedef int DRM_Err;

#define DRM_Err_Ok                  0
#define DRM_Err_Unkown             -1
#define DRM_Err_Algorithm_Invalid   1   /* transformation�����������㷨��֧�� */
#define DRM_Err_Buffer_Insufficient 2   /* ����������ĳ��Ȳ��� */
#define DRM_Err_Parameter_Invalid   3
#define DRM_Err_Key_Length_Invalid  4
#define DRM_Err_IV_Length_Invalid   5
#define DRM_Err_Padding_Invalid   6
#define DRM_Err_Secret_Length_Invalid   7 /* invalid crypted-text length */


#define IN
#define OUT
#define CAN_0 /* ��������ΪNULL/0 */


#ifdef WIN32
#ifdef WIN_DLL
#ifdef _EBOOK_DLL
#define EBOOK_DRM_API extern "C" __declspec(dllexport)
#else
#define EBOOK_DRM_API extern "C" __declspec(dllimport)
#endif //_EBOOK_DLL
#else
#define EBOOK_DRM_API extern "C"
#endif //WIN_DLL
#else  //Linux
#define EBOOK_DRM_API extern "C"
#endif //WIN32

#define OPMODE_ENCRYPT 0
#define OPMODE_DECRYPT 1

/***************************************************************************************************************************/
//˵����  �����ӽ��ܾ����
//������  pcipher         [out]  �ӽ��ܾ��
//        transformation  [in]   transformation: ���ܿ�ͨ��Ϊ�ַ�������ʽΪ "�㷨/Сģʽ/padding", ��"DES/CBC/PKCS-5Padding"��
//                               Ϊ�˷�ֹ�ַ���й¶��Ϣ�������ֶ������ִ��棬��2�͵�3����Ϊ0����û��ʱ��ʾȱʡģʽ��
//                               Ŀǰ֧��:
// "1/0/0": AES ECBģʽ��paddingΪPKCS-5
// "1/1/0": AES CBCģʽ��paddingΪPKCS-5
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err 
cipher_create(PCipher pcipher, const char* transformation);

/***************************************************************************************************************************/
//˵����  ���ɼӽ�����Կ��
//������    pcipher         [in]   �ӽ��ܾ��
//          key             [out]  �ӽ�����Կ���ڴ����ⲿ����,������ڴ�Ҫ�㹻�󣬷��򷵻�DRM_Err_Buffer_Insufficient�����룬
//                                 �ٰ���keylen�����ڴ档
//          keylen         [in/out]  �ӽ�����Կ����
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_generatekey(Cipher cipher, unsigned char* key, IN OUT int *keylen);

/***************************************************************************************************************************/
//˵����  ��ʼ���ӽ��ܡ�
//������  cipher          [in]   �ӽ��ܾ��
//        opmode          [in]   �ӽ���ģʽ������ΪOPMODE_ENCRYPT������ΪOPMODE_DECRYPT
//        key��Key_len    [in]   ��Կ��Ϣ
//        IV��IV_len      [in]   ��ʼ������
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err 
cipher_init(Cipher cipher, int opmode, 
			const unsigned char* key, int key_len,
			const unsigned char* IV,
			int IV_len);

/***************************************************************************************************************************/
//˵����  �������ݼӽ��ܡ�
//������  cipher                  [in]    �ӽ��ܾ��
//        in_data, in_data_len    [in]    ���������
//        out_data                [out]   ���������,�ڴ����ⲿ����
//        out_data_len            [in/out]������ݵĳ���
//����ֵ��DRM_Err                 ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_update(Cipher cipher, const unsigned char* in_data, int in_data_len, 
			  unsigned char* out_data, IN OUT int* out_data_len);

/***************************************************************************************************************************/
//˵����  �����ӽ���,�Խ�β����Ӧpadding����
//������  cipher                  [in]  �ӽ��ܾ��
//        in_data, in_data_len    [in]  ��������ݣ�����ΪNULL/0
//        out_data                [out] ��������ݣ��ڴ����ⲿ���룬������ڴ�Ҫ�㹻�󣬷��򷵻�DRM_Err_Buffer_Insufficient�����룬
//                                      �ٰ���out_data_len�����ڴ档
//        out_data_len            [in/out]������ݵĳ���
//����ֵ��DRM_Err                 ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_final(Cipher cipher, CAN_0 const unsigned char* in_data, CAN_0 int in_data_len, 
			 unsigned char* out_data, IN OUT int* out_data_len);

/***************************************************************************************************************************/
//˵����  �ͷżӽ��ܾ����
//������  cipher                 [in]  �ӽ��ܾ��
//����ֵ��DRM_Err                 ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_destroy(Cipher cipher);

/***************************************************************************************************************************/
//˵����  ����ժҪ�����
//������  pdigestor         [out]  ժҪ���
//        transformation    [in]   transformation: ���ܿ�ͨ��Ϊ�ַ�������ʽΪ "�㷨/Сģʽ/padding"
//����ֵ��DRM_Err           ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_create(PDigestor pdigestor, const char* transformation);

/***************************************************************************************************************************/
//˵����  ��������ժҪ��
//������  digestor          [in]      ժҪ���
//        data,data_len     [in]      ��������
//����ֵ��DRM_Err           ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_update(Digestor digestor, const unsigned char* data, int data_len);

/***************************************************************************************************************************/
//˵����  ����ժҪ��
//������  digestor              [in]    ժҪ���
//        data, data_len        [in]    ��������ݣ�����ΪNULL/0
//        digest                [out]   ���������
//        digest_len            [in/out]������ݵĳ���
//����ֵ��DRM_Err               ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_final(Digestor digestor, CAN_0 const unsigned char* data, CAN_0 int data_len, 
			   unsigned char* digest, IN OUT int* digest_len);

/***************************************************************************************************************************/
//˵����  �ͷ�ժҪ�����
//������  digestor                [in]  ժҪ���
//����ֵ��DRM_Err                 ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_destroy(Digestor digestor);

/*
cipher_stream_xxϵ�к�����Ϊ�˸������ļ��ṩһ�����ؽ���ϸ�ڵĽӿڡ�
**/
/***************************************************************************************************************************/
//˵����  ������ʽ�ӽ��ܾ����
//������  pstream         [out]  ��ʽ�ӽ��ܾ��
//        transformation  [in]   transformation: ���ܿ�ͨ��Ϊ�ַ�������ʽΪ "�㷨/Сģʽ/padding"
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_create(PCipherStream pstream, const char* transformation);


/***************************************************************************************************************************/
//˵����  ��ʼ����ʽ�ӽ��ܡ�
//������  pcipher         [in/out]  �ӽ��ܾ��
//        opmode          [in]   �ӽ���ģʽ������ΪOPMODE_ENCRYPT������ΪOPMODE_DECRYPT
//        key��Key_len    [in]   ��Կ��Ϣ
//        IV��IV_len      [in]   ��ʼ������
//        read_ptr        [in]   ���ļ�����ָ��
//        read_userp      [in]   ���ļ����ļ����
//        write_ptr       [in]   д�ļ�����ָ��
//        write_userp     [in]   д�ļ����ļ����
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_init(PCipherStream pstream, int opmode, 
				   const unsigned char* key, int key_len,
				   const unsigned char* IV, 	int IV_len,
				   read_proc read_ptr, void* read_userp,
				   write_proc write_ptr, void* write_userp
				   );

/***************************************************************************************************************************/
//˵����  �ú����ڲ�����read_ptr��ȡ���ݣ��ӽ�����Ӧ��������write_ptrд�롣
//������  pstream         [in/out]  ��ʽ�ӽ��ܾ��
//����ֵ��DRM_Err         ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_transform(PCipherStream pstream);

/***************************************************************************************************************************/
//˵����  �ͷ���ʽ�ӽ��ܾ����
//������  pstream        [in]  ��ʽ�ӽ��ܾ��
//����ֵ��DRM_Err        ������
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_destroy(PCipherStream pstream);

/*��ʽ�ӽ��ܵ���˵��
cipher_stream_xx������Ҫ���ݽ��м�/����ʱ����reader_ptr��ȡ��reade_ptr(read_userp, data, data_to_read); 
����cipher_stream_transform��ͬʱ����Ҫ�Ѽ�/���ܺ������д�룺write_ptr(write_userp, data, data_to_write).
reade_proc��write_proc�ķ���ֵ��ʵ�ʶ�д���ֽ�������read_ptrû�����ݿɶ�ʱ������EOF(-1)��
read_userp��write_userp�ֱ���Ϊread_ptr��write_ptr�Ĳ����������C�⣬���÷�ʽΪ:
  int __cdecl read_file(FILE* fp, void* buf, int buf_len)
  {
      return fread(buf, 1, buf_len, fp);
  }

  int __cdecl write_file(FILE* fp, void* buf, int buf_len)
  {
      return fwrite(buf, 1, buf_len, fp);
  }
  
	CipherStream stream;
	cipher_stream_create(&stream, "1");
	FILE * fp_read, *fp_write;
	cipher_stream_init(stream, OPMODE_DECRYPT, "12345678", 8, read_file, fp_read, write_file, fp_write);
*/

#endif //DRMALGORITHM_H_INCLUDED
