import java.io.*;
import java.util.*;

class PCB{
    int JID;
    int TTL;
    int TLL;

    PCB(int jid,int ttl,int tll)
    {
        this.JID = jid;
        this.TTL = ttl;
        this.TLL = tll;
    }
}

public class Phase2  {

    //    Memory access is only for OS functions
    private char M[][] = new char[300][4];

    //    Private Access to OS Function
    private char IR[] = new char[4];
    private int IC;
    private char R[] = new char[4];
    private boolean C;

    private int SI;

    private int PI;
    private int TI;
    private int EM;
    private int PTR;

    private int count;

    private int RA;

    private int TTC;
    private int TLC;
    private int ptrPointer;
    private ArrayList<Integer>AllocateList =  new ArrayList<>();

    private boolean isTerminated;

    private String input_File;
    private String output_File;

    private BufferedReader bReader;
    private BufferedWriter bWriter;

    private HashMap<Integer,Integer> VaToRa = new HashMap<>();

    PCB p1 = new PCB(0,0,0);

    Phase2(String inputfile, String outputfile) throws Exception
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
        System.out.println("Enter in Load Function");
        String Reader = bReader.readLine();
        System.out.println(Reader);



        while(Reader!=null)
        {

//          Loading Logic
//            Loading Control Card Data
            if(Reader.contains("$AMJ"))
            {
                INIT();
//                PID,TTL,TLL
                int temp[] = new int[3];
                int j =0;
                for(int i=4;i<Reader.length();i+=4)
                {
                    temp[j] = Integer.parseInt(Reader.substring(i,i+4));
                    j++;
                }

                p1 = new PCB(temp[0],temp[1],temp[2]);
                cardReader[0] = true;
            }
            else if(Reader.contains("$DTA"))
            {
                printPageTable(PTR);
                printPCB(p1);
                STARTEXECUTION();
                cardReader[1] = true;
            }
            else if(Reader.contains("$END"))
            {
                printMemory();


            }
            else if(!Reader.contains("$") && cardReader[0] && !cardReader[1])
            {
//                Reading Program Card
//                System.out.println(Reader);


                int loc = ALLOCATE();
                M[ptrPointer][0] = '1';
                M[ptrPointer][2] = (char)((loc/10)+'0');
                M[ptrPointer][3] = (char)((loc%10)+'0');
                ptrPointer++;

                int row = loc*10;
                int col = 0;

                if(Reader.length()>40)
                {
                    Reader = Reader.substring(0,40);
                }
                for(char i : Reader.toCharArray())
                {
                    if(row<300) {
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
        this.SI = 0;
        this.EM = -1;
        this.PI = 0;
        this.TI = 0;
        this.PTR = 0;
        this.TLC = 0;
        this.TTC = 0;
        this.RA = 0;
        this.AllocateList.clear();
        this.PTR = ALLOCATE()*10;
//      Initialization of Page Table
        for(int i = PTR;i<PTR+10;i++)
        {
            M[i][0] = '0';
            M[i][2] = '*';
            M[i][3] = '*';
        }
        ptrPointer = PTR;
        this.p1 = new PCB(0,0,0);
        this.VaToRa.clear();
        count=0;
        this.isTerminated = false;
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
            ADDRESSMAP(IC);
//            Loading Instruction in IR
            int j = 0;
            for(char i : M[RA])
            {
                IR[j] = i;
                j++;
            }
//            Increment Instruction Counter by 1
            IC = IC + 1;
            int operand = (int)(IR[2]-'0')*10 + (int)(IR[3]-'0');
            System.out.println("operand" +operand);
            MAP (operand);
            operand = RA;
            System.out.println("operand" +operand);


//            Separating Operand and Opcode

            StringBuilder opcode = new StringBuilder();
            if(IR[0]=='H')
            {
                opcode.append(IR[0]);
            }
            else {
                opcode.append(IR[0]);
                opcode.append(IR[1]);
            }

            System.out.println("Opcode  : "+opcode);
            System.out.println("Operand  : "+operand);

            switch (opcode.toString())
            {
                case "GD" :
                    SI = 1;
                    break;
                case "PD":
                    SI = 2;
                    break;
                case "H":
                    SI = 3;
                    loop = false;
                    break;
                case "LR":
//                    Storing the data from Memory -----> Register
//                    if(TTC>p1.TTL)
//                    {
//                       TI = 2;
//                        loop=false;
//                        return;
//                    }
                    j=0;
                    for(char i : M[operand])
                    {
                        R[j] = i;
                        j++;
                    }
                    System.out.println(Arrays.toString(R));
                    break;
                case "SR":
//                    Loading the data from Register ----> Memory
//                    if(TTC>p1.TTL)
//                    {
//                        TI = 2;
//                        loop=false;
//                        return;
//                    }
                    j = 0;
                    for(char i : R)
                    {
                        M[operand][j] = i;
                        j++;
                    }
                    break;
                case "CR":
//                    Comparing the data of Register -----> Memory
//                    if(TTC>p1.TTL)
//                    {
//                        TI = 2;
//                        loop=false;
//                        return;
//                    }
                    int c = 0;
                    j=0;
                    for(char i : R)
                    {
                        if(M[operand][j] == i)
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
//                    if(TTC>p1.TTL)
//                    {
//                        TI = 2;
//                        loop=false;
//                        return;
//                    }
                    if(C) {
                        this.IC = operand;
                        System.out.println("IC = "+IC);
                    }
                    break;
                default:
                    System.out.println("Invalide Command Or Command Not Found");
                    PI = 1;
                    break;
            }
            SIMULATION();
        if(SI!=0 || PI!=0 || TI!=0) {
            System.out.println("SI = "+SI);
            System.out.println("PI = "+PI);
            System.out.println("TI = "+TI);
            if(!this.isTerminated) {
                if (MOS(operand) == -1) {
                    loop = false;
                }
            }
            else{
                loop=false;
            }
            SI=0;
            PI=0;
            TI=0;

        }

        }

    }

    private void SIMULATION() {
        TTC++;
        System.out.println("TTC = "+TTC);

        if(TTC>p1.TTL)
        {
            TI = 2;
        }
        System.out.println(TI);
    }

    private int MOS(int operand) throws Exception
    {
        if(TI==0)
        {
            if(PI==1)
            {
                TERMINATE(4);
                return -1;
            }else if(PI==2)
            {
                TERMINATE(5);
                return -1;
            }else if(PI==3)
            {
                if((IR[0]=='G' && IR[1]=='D') || (IR[0]=='S' && IR[1]=='R') ){
                    int temp = ALLOCATE();
                    VaToRa.put(operand,temp);
                    // printf("Allocated memory block is %d for %d operand\n\n\n",value[key_index] ,key[key_index]);
                    M[ptrPointer][0]='1';
                    M[ptrPointer][3]=(char)(temp%10+'0');
                    M[ptrPointer][2]=(char)(temp/10+'0');
                    ptrPointer++;
                    RA = VaToRa.get(operand)*10 + operand%10;
                    PI=0;
                    return 1;
                }
                else {
                    TERMINATE(6);
                    this.isTerminated=true;
                    return -1;
                }

            }
            else if(SI==1)
            {
                return READ(operand);
            }else if(SI==2)
            {
                return WRITE(operand);
            }else if(SI==3)
            {
                TERMINATE(0);
                return -1;
            }
        }else if(TI==2)
        {
            if(PI==1)
            {
                TERMINATE(7);
                return -1;
            }else if(PI==2)
            {
                TERMINATE(8);
                return -1;
            }else if(PI==3)
            {
                TERMINATE(6);
                return -1;
            }
           else if(SI==1)
            {
                    TERMINATE(3);
                    return -1;
            }else if(SI==2)
            {
                WRITE(operand);
                TERMINATE(3);
                return -1;
            }else if(SI==3)
            {
                TERMINATE(0);
                return -1;
            }
           else {
               TERMINATE(3);
               return -1;
            }
        }

        return 1;

    }

    private int READ(int location) throws Exception
    {
        String Data = bReader.readLine();
        if(Data.contains("$END"))
        {
            TERMINATE(1);
            return -1;
        }else {
            System.out.println(Data);
            int col = 0;
            for (char i : Data.toCharArray()) {
                M[location][col % 4] = i;
                col++;
                if (col % 4 == 0) {
                    location++;
                }
                if (location > 299) {
                    System.out.println("Memory Exceed! "+location);
                    break;
                }
            }
        }
        return 1;
    }

    private int WRITE(int location) throws Exception
    {
        TLC++;
        System.out.println("TLC = "+TLC);
        if(TLC>p1.TLL)
        {
            TERMINATE(2);
            return -1;
        }else {
            int col = 0;
            char i = M[location][col];
            StringBuilder Data = new StringBuilder();
            int j = location;
            while (j < location + 10) {
                Data.append(i);
                col++;
                if (col % 4 == 0) {
                    j++;
                }
                if (j > 299) {
                    System.out.println("Memory Exceed! "+ j);
                    break;
                }
                i = M[j][col % 4];
            }
            bWriter.write(Data.toString());
            bWriter.newLine();
        }

        return 1;
    }

    private void TERMINATE(int EM) throws Exception
    {

        String error = "";
        switch (EM)
        {
            case 0:
                error = "No Error";
                break;
            case 1:
                error = "Out of Data";
                break;
            case 2:
                error = "Line Limit Exceeded";
                break;
            case 3:
                error = "Time Limit Exceeded";
                break;
            case 4:
                error = "Operation Code Error";
                break;
            case 5:
                    error="Operand Error";
                    break;
            case 6:
                error = "Invalid Page Fault";
                break;
            case 7:
                error = "Time Limit Exceed + Operation Code Error";
                break;
            case 8:
                error = "Time Limit Exceed + Operand Error";
                break;
            default:
                System.out.println("Invalide Error Message");
        }

        bWriter.write("JOB ID \t:\t"+p1.JID);
        bWriter.newLine();
        bWriter.write(error);
        bWriter.newLine();
        bWriter.write("IC \t\t\t:\t"+IC);
        bWriter.newLine();
        bWriter.write("IR \t\t\t:\t"+Arrays.toString(IR));
        bWriter.newLine();
        bWriter.write("TTC \t\t:\t"+TTC);
        bWriter.newLine();
        bWriter.write("TLC \t\t:\t"+TLC);
        bWriter.write("\n");
        bWriter.write("\n");
    }

    private int ALLOCATE()
    {
        Random rand = new Random();
        int value;

        // Generate a random value between 0 and 29 and ensure it is not in the list
        do {
            value = rand.nextInt(30); // Generates a number between 0 and 29
        } while (AllocateList.contains(value));

        // Add the value to the allocateList to track it
        AllocateList.add(value);
        return value;
    }


//    printing PTR
    private void printPageTable(int ptr)
    {
        System.out.println("Page Table");
        for(int i=ptr;i<ptr+10;i++)
        {
            System.out.println(i+" "+Arrays.toString(M[i]));
        }
    }

//    printing PCB
    private void printPCB(PCB p1)
    {
        System.out.println("JID : "+p1.JID);
        System.out.println("TTL : "+p1.TTL);
        System.out.println("TLL : "+p1.TLL);
    }



//    Address Map
private void ADDRESSMAP(int IC)
{
    if(IC%10 == 0 && IC!=0)
    {
        count++;
    }
    int address=(int)(M[PTR+count][2]-'0')*10+(int)(M[PTR+count][3]-'0');

    address=address*10+IC%10;

    RA = address;

}


// Add into MAP
    private void MAP(int add) throws Exception
    {
        if((int)(IR[2]-'0')<0 || (int)(IR[2]-'0')>9 || (int)(IR[3]-'0')<0 || (int)(IR[3]-'0')>9){
            if(IR[0]!='H') {
                PI = 2;
                return;
            }
            RA=-1;
            return;
        }
        if(IR[0]=='B' && IR[1]=='T'){
            System.out.println("This is BT");
            RA = add;
            return;
        }
        
        if(VaToRa.containsKey((add/10)*10))
        {
            RA = VaToRa.get((add/10)*10)*10+(add%10);
            System.out.println("from map");
            return;
        }
//        change by mos

        else{
            PI=3;
//            return;
            int a = MOS(add);
        }
    }




    public static void main(String[] args) throws Exception {
        String InputFile = "O:\\OS\\Phase1\\My Phase\\Phase 2\\input.txt";
        String OutputFile = "O:\\OS\\Phase1\\My Phase\\Phase 2\\output.txt";

        Phase2 p1 = new Phase2(InputFile,OutputFile);

        p1.INIT();
        p1.LOAD();
        p1.bWriter.close();
    }
}
