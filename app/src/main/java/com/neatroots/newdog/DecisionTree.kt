package com.neatroots.newdog


class DecisionTree {

    fun createTree(): DecisionNode {
        return DecisionNode(
            question = "มีไข้หรือไม่?",
            yesNode = DecisionNode(
                question = "มีอาการซึมหรือไม่?",
                yesNode = DecisionNode(
                    question = "อาเจียนหรือไม่?",
                    yesNode = DecisionNode(
                        question = "เบื่ออาหารหรือไม่?",
                        yesNode = DecisionNode(
                            question = "ท้องเสียหรือไม่?",
                            yesNode = DecisionNode(
                                question = "หายใจลำบากหรือไม่?",
                                yesNode = DecisionNode(
                                    question = "มีน้ำมูกหรือไม่?",
                                    yesNode = DecisionNode(
                                        question = "มีขี้ตาหรือไม่?",
                                        yesNode = DecisionNode(
                                            diagnosis = "ไข้หัดสุนัข",
                                            details = "เป็นโรคติดต่อร้ายแรงจากเชื้อไวรัส Morbillivirus ติดต่อผ่านการหายใจหรือสัมผัสสิ่งคัดหลั่ง",
                                            symptomsAndCauses = "สาเหตุ: เกิดจากเชื้อไวรัส Morbillivirus ติดต่อผ่านการหายใจหรือสัมผัสน้ำมูก น้ำลาย น้ำตา อุจจาระ ปัสสาวะ\nอาการ:\n1. ไข้สูง ซึม อ่อนแรง เบื่ออาหาร\n2. น้ำมูก ตาแฉะ ไอ จาม หายใจลำบาก\n3. อาเจียน ท้องเสีย\n4. ตุ่มหนองที่ผิวหนัง ผิวหนังอุ้งเท้าหนา (เรื้อรัง)\n5. อาการทางประสาท เช่น ชัก กล้ามเนื้อกระตุก",
                                            careInstructions = "1. แยกสุนัขป่วยออกจากตัวอื่นทันที\n2. รีบพาไปพบสัตวแพทย์เพื่อรักษาตามอาการ เช่น ยาปฏิชีวนะ น้ำเกลือ หรือยาสงบประสาท\n3. ให้พักผ่อนในที่เงียบสงบและอบอุ่น\n4. ให้อาหารอ่อนย่อยง่ายและน้ำสะอาด\n5. ทำความสะอาดน้ำมูกและน้ำตา"
                                        ),
                                        noNode = DecisionNode(
                                            diagnosis = "ลำไส้อักเสบติดต่อจากเชื้อพาโรไวรัส",
                                            details = "เป็นโรคติดต่อร้ายแรงจากเชื้อพาร์โวไวรัส ติดต่อผ่านการสัมผัสอุจจาระหรือพื้นผิวที่ปนเปื้อน",
                                            symptomsAndCauses = "สาเหตุ: เกิดจากเชื้อพาร์โวไวรัส ติดต่อผ่านอุจจาระหรือพื้นผิวที่ปนเปื้อน\nอาการ:\n1. เบื่ออาหาร อาเจียน อ่อนเพลีย\n2. ท้องเสียรุนแรง อุจจาระมีเลือดปน\n3. มีไข้ ภาวะขาดน้ำ ปวดท้อง",
                                            careInstructions = "1. รีบพาไปโรงพยาบาลสัตว์เพื่อให้สารน้ำทางหลอดเลือดและยาลดอาเจียน\n2. งดอาหาร 24 ชม. แต่ให้น้ำเกลือแร่ป้องกันขาดน้ำ\n3. แยกจากสุนัขตัวอื่น\n4. ทำความสะอาดด้วยสารฟอกขาวเจือจาง"
                                        )
                                    ),
                                    noNode = DecisionNode(
                                        diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                                        details = "ควรพาสุนัขไปพบสัตวแพทย์",
                                        symptomsAndCauses = "ไม่มีข้อมูล",
                                        careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                                    )
                                ),
                                noNode = DecisionNode(
                                    diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                                    details = "ควรพาสุนัขไปพบสัตวแพทย์",
                                    symptomsAndCauses = "ไม่มีข้อมูล",
                                    careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                                )
                            ),
                            noNode = DecisionNode(
                                question = "มีน้ำมูกหรือไม่?",
                                yesNode = DecisionNode(
                                    question = "ไอหรือไม่?",
                                    yesNode = DecisionNode(
                                        diagnosis = "ไข้หวัดใหญ่ในสุนัข",
                                        details = "เกิดจากไวรัสไข้หวัดใหญ่ชนิด A (H3N2 หรือ H3N8) ติดต่อผ่านละอองในอากาศ",
                                        symptomsAndCauses = "สาเหตุ: เกิดจากไวรัสไข้หวัดใหญ่ชนิด A (H3N2, H3N8) ติดต่อผ่านละอองในอากาศ\nอาการ:\n1. จาม น้ำมูกใสปนหนอง ไอ\n2. มีไข้ ซึม เบื่ออาหาร",
                                        careInstructions = "1. ให้พักผ่อนในที่แห้งและอบอุ่น\n2. ให้น้ำและอาหารเพียงพอ\n3. แยกจากสุนัขตัวอื่น\n4. หากอาการรุนแรง (น้ำมูกปนหนองหรือปอดอักเสบ) ให้พบสัตวแพทย์เพื่อรับยาปฏิชีวนะ"
                                    ),
                                    noNode = DecisionNode(
                                        question = "หายใจลำบากหรือไม่?",
                                        yesNode = DecisionNode(
                                            diagnosis = "หลอดลมอักเสบในสุนัข",
                                            details = "เกิดจากเชื้อไวรัสและแบคทีเรีย เช่น Bordetella bronchiseptica ติดต่อผ่านละอองในอากาศ",
                                            symptomsAndCauses = "สาเหตุ: เกิดจากเชื้อ Bordetella bronchiseptica, Parainfluenza Virus ฯลฯ ติดต่อผ่านละอองในอากาศหรือการสัมผัส\nอาการ:\n1. ไอรุนแรง (เหมือนห่านร้อง)\n2. น้ำมูกไหล จาม\n3. อ่อนเพลีย มีไข้ต่ำ",
                                            careInstructions = "1. ให้พักผ่อนและดื่มน้ำเพียงพอ\n2. ใช้เครื่องทำความชื้นบรรเทาอาการไอ\n3. แยกจากสุนัขตัวอื่น\n4. หากอาการรุนแรง ให้พบสัตวแพทย์เพื่อรับยาปฏิชีวนะ"
                                        ),
                                        noNode = DecisionNode(
                                            diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                                            details = "ควรพาสุนัขไปพบสัตวแพทย์",
                                            symptomsAndCauses = "ไม่มีข้อมูล",
                                            careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                                        )
                                    )
                                ),
                                noNode = DecisionNode(
                                    diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                                    details = "ควรพาสุนัขไปพบสัตวแพทย์",
                                    symptomsAndCauses = "ไม่มีข้อมูล",
                                    careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                                )
                            )
                        ),
                        noNode = DecisionNode(
                            question = "มีน้ำมูกหรือไม่?",
                            yesNode = DecisionNode(
                                diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                                details = "ควรพาสุนัขไปพบสัตวแพทย์",
                                symptomsAndCauses = "ไม่มีข้อมูล",
                                careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                            ),
                            noNode = DecisionNode(
                                diagnosis = "ไตวายเรื้อรัง",
                                details = "เกิดจากการเสื่อมของไตตามอายุหรือปัจจัยอื่นๆ ไม่สามารถรักษาหายขาดได้",
                                symptomsAndCauses = "สาเหตุ: อายุมาก สายพันธุ์เสี่ยง ยา โรคอื่นๆ อาหารไม่เหมาะสม\nอาการ:\n1. เบื่ออาหาร น้ำหนักลด\n2. ซึม ขนร่วง\n3. ปัสสาวะบ่อยหรือไม่ปัสสาวะ ดื่มน้ำเยอะ\n4. กลิ่นปาก ท้องเสีย เหงือกซีด",
                                careInstructions = "1. ควบคุมอาหารสูตรไต (โปรตีนต่ำ โซเดียมต่ำ)\n2. ให้น้ำสะอาดตลอดเวลา\n3. หลีกเลี่ยงความเครียด\n4. พบสัตวแพทย์ตามนัด"
                            )
                        )
                    )
                ),
                noNode = DecisionNode(
                    diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                    details = "ควรพาสุนัขไปพบสัตวแพทย์",
                    symptomsAndCauses = "ไม่มีข้อมูล",
                    careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
                )
            ),
            noNode = DecisionNode(
                diagnosis = "ไม่พบโรคที่เกี่ยวข้อง",
                details = "ควรพาสุนัขไปพบสัตวแพทย์",
                symptomsAndCauses = "ไม่มีข้อมูล",
                careInstructions = "สังเกตอาการเพิ่มเติมและพาไปพบสัตวแพทย์"
            )
        )
    }

    fun analyze(tree: DecisionNode, userAnswer: (String) -> Boolean): String {
        var currentNode = tree
        while (currentNode.question != null) {
            val answer = userAnswer(currentNode.question!!)
            currentNode = if (answer) currentNode.yesNode!! else currentNode.noNode!!
        }
        return currentNode.diagnosis ?: "ไม่สามารถวิเคราะห์ได้"
    }
}