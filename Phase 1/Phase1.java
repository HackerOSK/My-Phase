import java.io.*;
import java.util.Arrays;

public class Phase1  {

//    Memory access is only for OS functions
    private char M[][] = new char[100][4];

//    Private Access to OS Function
    private char IR[] = new char[4];
    private int IC;
    private char R[] = new char[4];
    private boolean C;

    private int PID;
    private int TTL;
    private int TLL;

    private int SI;

    private String input_File;
    private String output_File;

    private BufferedReader bReader;
    private BufferedWriter bWriter;

    Phase1(String inputfile, String outputfile) throws Exception
    {
        this.input_File = inputfile;
        this.output_File = outputfile;
        File fileR = new File(input_File);
        File fileW = new File(output_File);
        bReader = new BufferedReader(new FileReader(fileR));
        bWriter = new BufferedWriter(new FileWriter(fileW));
    }



    private boolean cardReader[] = new boolean[2];
//    0 = Control card  ||  1 = Data card

// Private Functions

//    1. LOAD function
    private void LOAD() throws Exception
    {

        String Reader = bReader.readLine();
        int row = 0;
        int col = 0;
        while(Reader!=null)
        {
//            System.out.println(Reader);
//          Loading Logic
//            Loading Control Card Data
            if(Reader.contains("$AMJ"))
            {
//                PID,TTL,TLL
                int temp[] = new int[3];
                int j =0;
                for(int i=4;i<Reader.length();i+=4)
                {
                    temp[j] = Integer.parseInt(Reader.substring(i,i+4));
                }
                PID = temp[0];
                TTL = temp[1];
                TLL = temp[2];
                cardReader[0] = true;
            }
            else if(Reader.contains("$DTA"))
            {
                STARTEXECUTION();
                cardReader[1] = true;
            }
            else if(Reader.contains("$END"))
            {
                printMemory();
                INIT();
                row=0;
                col =0;
            }
            else if(!Reader.contains("$") && cardReader[0] && !cardReader[1])
            {
//                Reading Program Card
//                System.out.println(Reader);

                if(Reader.length()>40)
                {
                    Reader = Reader.substring(0,40);
                }
                for(char i : Reader.toCharArray())
                {
                    if(row<100) {
                        M[row][col % 4] = i;
                        col++;
                    }else{
                        System.out.println("Memory Limit Exceed!!");
                    }
                    if(col%4==0)
                    {
                        row++;
                    }
                }
            }


            Reader = bReader.readLine();
        }

    }

//    2. INIT function
    private void INIT()
    {
        for(char arr[] : this.M)
        {
            Arrays.fill(arr,' ');
        }

        this.IC = 0;
        Arrays.fill(this.R,' ');
        Arrays.fill(this.IR,' ');
        this.C = false;
        Arrays.fill(this.cardReader,false);
        this.SI = -1;
    }

//    3. Print Memory
    private void printMemory()
    {
        for(int i = 0;i<M.length;i++)
        {
            System.out.println(i+" "+Arrays.toString(M[i]));
        }
    }


//    4. STARTEXECUTION program
    private void STARTEXECUTION() throws Exception
    {
        this.IC = 0;
        EXECUTEUSERPROGRAM();
    }

//    5. EXECUTEUSERPROGRAM

    private void EXECUTEUSERPROGRAM() throws Exception
    {
        boolean loop = true;
        while(loop)
        {
//            Loading Instruction in IR
            int j = 0;
            for(char i : M[IC])
            {
                IR[j] = i;
                j++;
            }
//            Increment Instruction Counter by 1
            IC = IC + 1;

//            Separating Operand and Opcode
            StringBuilder operand = new StringBuilder();
            StringBuilder opcode = new StringBuilder();
            if(IR[0]=='H')
            {
                opcode.append(IR[0]);
            }
            else {
                opcode.append(IR[0]);
                opcode.append(IR[1]);
                operand.append(IR[2]);
                operand.append(IR[3]);
            }

            System.out.println("Opcode  : "+opcode);
            System.out.println("Operand  : "+operand);

            switch (opcode.toString())
            {
                case "GD" :
                    SI = 1;
                    MOS(Integer.parseInt(operand.toString()));
                    break;
                case "PD":
                    SI = 2;
                    MOS(Integer.parseInt(operand.toString()));
                    break;
                case "H":
                    SI = 3;
                    MOS(0);
                    loop = false;
                    break;
                case "LR":
//                    Storing the data from Memory -----> Register
                    j=0;
                    for(char i : M[Integer.parseInt(operand.toString())])
                    {
                        R[j] = i;
                        j++;
                    }
                    System.out.println(Arrays.toString(R));
                    break;
                case "SR":
//                    Loading the data from Register ----> Memory
                    j = 0;
                    for(char i : R)
                    {
                        M[Integer.parseInt(operand.toString())][j] = i;
                        j++;
                    }
                    break;
                case "CR":
//                    Comparing the data of Register -----> Memory
                    int c = 0;
                    j=0;
                    for(char i : R)
                    {
                        if(M[Integer.parseInt(operand.toString())][j] == i)
                        {
                            c++;
                        }
                        j++;
                    }
                    System.out.println(c);
//                    System.out.println(Arrays.toString(M[Integer.parseInt(operand.toString())]));
//                    System.out.println(Arrays.toString(R));
                    if(c==4)
                    {
                        this.C = true;
                    }
                    break;
                case "BT":
                    if(C) {
                        this.IC = Integer.parseInt(operand.toString());
                        System.out.println("IC = "+IC);
                    }
                    break;
                default:
                    System.out.println("Invalide Command Or Command Not Found");
                    break;
            }
        }

    }

    private void MOS(int operand) throws Exception
    {
        switch (SI)
        {
            case 1:
                READ(operand);
                break;
            case 2:
                WRITE(operand);
                break;
            case 3:
                TERMINATE();
                break;
            default:
                System.out.println("Invalide MOS!");
        }
    }

    private void READ(int location) throws Exception
    {
        String Data = bReader.readLine();
        System.out.println(Data);
        int col = 0;
        for(char i : Data.toCharArray())
        {
            M[location][col%4] = i;
            col++;
            if(col%4==0)
            {
                location++;
            }
            if(location>99)
            {
                System.out.println("Memory Exceed!");
                break;
            }
        }
    }

    private void WRITE(int location) throws Exception
    {
        int col = 0;
        char i = M[location][col];
        StringBuilder Data = new StringBuilder();
        int j = location;
        while(j<location+10)
        {
            Data.append(i);
            col++;
            if(col%4==0)
            {
                j++;
            }
            if(j>99)
            {
                System.out.println("Memory Exceed!");
                break;
            }
            i = M[j][col%4];
        }
        bWriter.write(Data.toString());
        bWriter.newLine();
    }

    private void TERMINATE() throws Exception
    {
        bWriter.write("\n");
        bWriter.write("\n");
    }


    public static void main(String[] args) throws Exception {
        String InputFile = "O:\\OS\\Phase1\\My Phase\\Phase 1\\Input.txt";
        String OutputFile = "O:\\OS\\Phase1\\My Phase\\Phase 1\\output.txt";
        Phase1 p1 = new Phase1(InputFile,OutputFile);
        p1.INIT();
        p1.LOAD();
        p1.bWriter.close();
    }
}
